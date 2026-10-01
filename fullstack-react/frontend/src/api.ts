import type { Account } from './types'

// Vite 프록시가 /api → http://localhost:8080 로 전달한다.
const BASE = '/api/accounts'

// 공통 응답 처리: 오류면 백엔드가 준 {error} 메시지로 예외를 던진다.
async function handle<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const data = await res.json().catch(() => ({ error: `요청 실패 (${res.status})` }))
    throw new Error(data.error ?? `요청 실패 (${res.status})`)
  }
  return (await res.json()) as T
}

const jsonHeaders = { 'Content-Type': 'application/json' }

export function getAccounts(): Promise<Account[]> {
  return fetch(BASE).then((r) => handle<Account[]>(r))
}

export function createAccount(owner: string, initialBalance: number): Promise<Account> {
  return fetch(BASE, {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ owner, initialBalance }),
  }).then((r) => handle<Account>(r))
}

export function deposit(no: string, amount: number): Promise<Account> {
  return fetch(`${BASE}/${no}/deposit`, {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ amount }),
  }).then((r) => handle<Account>(r))
}

export function withdraw(no: string, amount: number): Promise<Account> {
  return fetch(`${BASE}/${no}/withdraw`, {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ amount }),
  }).then((r) => handle<Account>(r))
}

export function transfer(from: string, to: string, amount: number): Promise<unknown> {
  return fetch(`${BASE}/transfer`, {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ from, to, amount }),
  }).then((r) => handle<unknown>(r))
}

export function closeAccount(no: string): Promise<unknown> {
  return fetch(`${BASE}/${no}`, { method: 'DELETE' }).then((r) => handle<unknown>(r))
}
