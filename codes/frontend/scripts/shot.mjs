#!/usr/bin/env node
/**
 * 视觉验收工具：用系统 Chrome + CDP 截图，并输出布局度量。
 *
 * 不依赖 Playwright / Puppeteer（Node 22 原生 WebSocket 即可驱动 CDP）。
 *
 * 用法：
 *   node scripts/shot.mjs --url=http://localhost:5173/ --out=../shots/home.png --w=1440 --h=900
 *   node scripts/shot.mjs --url=... --w=390 --h=844 --token=xxx --user='{"id":"1"}'
 *
 * 参数：
 *   --url     目标地址（必填）
 *   --out     输出 png 路径（可选，不传则只打印度量）
 *   --w/--h   视口尺寸，默认 1440x900
 *   --token   写入 localStorage.mall_token（用于登录态页面）
 *   --user    写入 localStorage.mall_user（JSON 字符串）
 *   --wait    导航后额外等待毫秒，默认 900
 */
import { spawn } from 'node:child_process'
import { mkdtempSync, writeFileSync, mkdirSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join, dirname } from 'node:path'

const CHROME = 'C:/Program Files/Google/Chrome/Application/chrome.exe'
const PORT = 9333 + Math.floor(Math.random() * 200)

const args = Object.fromEntries(
  process.argv.slice(2).map((item) => {
    const [key, ...rest] = item.replace(/^--/, '').split('=')
    return [key, rest.join('=')]
  })
)

const url = args.url
if (!url) {
  console.error('missing --url')
  process.exit(1)
}
const width = Number(args.w || 1440)
const height = Number(args.h || 900)
const waitMs = Number(args.wait || 900)

const profile = mkdtempSync(join(tmpdir(), 'mall-shot-'))
const chrome = spawn(CHROME, [
  '--headless=new',
  '--disable-gpu',
  '--hide-scrollbars',
  '--no-first-run',
  '--no-default-browser-check',
  `--user-data-dir=${profile}`,
  `--remote-debugging-port=${PORT}`,
  'about:blank'
])

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

async function waitForDevtools() {
  for (let i = 0; i < 60; i += 1) {
    try {
      const res = await fetch(`http://127.0.0.1:${PORT}/json/version`)
      if (res.ok) return
    } catch {
      /* not ready */
    }
    await sleep(150)
  }
  throw new Error('devtools not reachable')
}

async function openTarget() {
  const res = await fetch(`http://127.0.0.1:${PORT}/json/new?about:blank`, { method: 'PUT' })
  const data = await res.json()
  return data.webSocketDebuggerUrl
}

function createClient(wsUrl) {
  const ws = new WebSocket(wsUrl)
  let seq = 0
  const pending = new Map()
  const listeners = new Map()

  ws.addEventListener('message', (event) => {
    const msg = JSON.parse(event.data)
    if (msg.id && pending.has(msg.id)) {
      const { resolve, reject } = pending.get(msg.id)
      pending.delete(msg.id)
      if (msg.error) reject(new Error(msg.error.message))
      else resolve(msg.result)
      return
    }
    if (msg.method && listeners.has(msg.method)) {
      listeners.get(msg.method).forEach((fn) => fn(msg.params))
    }
  })

  const ready = new Promise((resolve, reject) => {
    ws.addEventListener('open', resolve)
    ws.addEventListener('error', reject)
  })

  return {
    ready,
    send(method, params = {}) {
      const id = (seq += 1)
      ws.send(JSON.stringify({ id, method, params }))
      return new Promise((resolve, reject) => pending.set(id, { resolve, reject }))
    },
    once(method) {
      return new Promise((resolve) => {
        const handler = (params) => {
          listeners.get(method).delete(handler)
          resolve(params)
        }
        if (!listeners.has(method)) listeners.set(method, new Set())
        listeners.get(method).add(handler)
      })
    },
    close() {
      ws.close()
    }
  }
}

try {
  await waitForDevtools()
  const client = createClient(await openTarget())
  await client.ready
  await client.send('Page.enable')
  await client.send('Runtime.enable')
  await client.send('Emulation.setDeviceMetricsOverride', {
    width,
    height,
    deviceScaleFactor: 1,
    mobile: width < 600
  })

  const loaded = client.once('Page.loadEventFired')
  await client.send('Page.navigate', { url })
  await loaded
  await sleep(waitMs)

  // 需要登录态时：在当前 origin 写入 localStorage，再重新导航
  if (args.token) {
    await client.send('Runtime.evaluate', {
      expression: `localStorage.setItem('mall_token', ${JSON.stringify(args.token)});${
        args.user ? `localStorage.setItem('mall_user', ${JSON.stringify(args.user)});` : ''
      }`
    })
    const reloaded = client.once('Page.loadEventFired')
    await client.send('Page.reload', { ignoreCache: false })
    await reloaded
    await sleep(waitMs)
  }

  const metrics = await client.send('Runtime.evaluate', {
    expression: `(() => {
      const doc = document.documentElement;
      const box = (sel) => {
        const el = document.querySelector(sel);
        if (!el) return null;
        const r = el.getBoundingClientRect();
        return { w: Math.round(r.width), h: Math.round(r.height), x: Math.round(r.x) };
      };
      return {
        viewport: [window.innerWidth, window.innerHeight],
        scrollWidth: doc.scrollWidth,
        overflowX: doc.scrollWidth - window.innerWidth,
        header: box('.header') || box('.topbar'),
        container: box('.mall-container'),
        grid: box('.product-grid'),
        card: box('.product-card'),
        errors: window.__mallErrors || []
      };
    })()`,
    returnByValue: true
  })

  console.log(JSON.stringify(metrics.result.value, null, 2))

  if (args.out) {
    const shot = await client.send('Page.captureScreenshot', { format: 'png' })
    const outPath = args.out
    mkdirSync(dirname(outPath), { recursive: true })
    writeFileSync(outPath, Buffer.from(shot.data, 'base64'))
    console.log(`saved: ${outPath}`)
  }

  client.close()
} finally {
  chrome.kill()
}
