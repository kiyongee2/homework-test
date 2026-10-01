import { useEffect, useState, type FormEvent } from 'react'
import type { Account } from './types'
import * as api from './api'

export default function App() {
  const [accounts, setAccounts] = useState<Account[]>([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  // 계좌 목록 새로 불러오기
  async function reload() {
    try {
      setAccounts(await api.getAccounts())
    } catch (e) {
      fail(e)
    }
  }

  useEffect(() => {
    reload()
  }, [])

  function notify(msg: string) {
    setMessage(msg)
    setError('')
  }
  function fail(e: unknown) {
    setError(e instanceof Error ? e.message : '알 수 없는 오류')
    setMessage('')
  }

  // 폼 제출을 공통 처리: 성공 시 메시지 + 목록 갱신 + 폼 초기화
  async function submit(
    e: FormEvent<HTMLFormElement>,
    run: (data: FormData) => Promise<string>,
  ) {
    e.preventDefault()
    const form = e.currentTarget
    try {
      const msg = await run(new FormData(form))
      notify(msg)
      form.reset()
      await reload()
    } catch (err) {
      fail(err)
    }
  }

  const str = (d: FormData, k: string) => String(d.get(k) ?? '').trim()
  const num = (d: FormData, k: string) => Number(d.get(k))

  return (
    <div className="wrap">
      <h1>
        🏦 은행 계좌 관리 <small>React + TypeScript · Spring Boot · H2</small>
      </h1>

      {message && <div className="msg ok">{message}</div>}
      {error && <div className="msg err">{error}</div>}

      <h2>계좌 목록</h2>
      <table>
        <thead>
          <tr>
            <th>계좌번호</th>
            <th>예금주</th>
            <th className="r">잔액</th>
            <th className="c">관리</th>
          </tr>
        </thead>
        <tbody>
          {accounts.map((a) => (
            <tr key={a.accountNumber}>
              <td>{a.accountNumber}</td>
              <td>{a.owner}</td>
              <td className="r">{a.balance.toLocaleString()}원</td>
              <td className="c">
                <button
                  className="danger"
                  onClick={async () => {
                    if (!confirm(`${a.accountNumber} 계좌를 해지할까요?`)) return
                    try {
                      await api.closeAccount(a.accountNumber)
                      notify(`${a.accountNumber} 계좌가 해지되었습니다.`)
                      await reload()
                    } catch (e) {
                      fail(e)
                    }
                  }}
                >
                  해지
                </button>
              </td>
            </tr>
          ))}
          {accounts.length === 0 && (
            <tr>
              <td colSpan={4} className="empty">
                등록된 계좌가 없습니다.
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <div className="cards">
        <div className="card">
          <h3>➕ 계좌 개설</h3>
          <form
            onSubmit={(e) =>
              submit(e, async (d) => {
                const a = await api.createAccount(str(d, 'owner'), num(d, 'initialBalance'))
                return `${a.accountNumber} 계좌가 개설되었습니다.`
              })
            }
          >
            <label>
              예금주 <input name="owner" required />
            </label>
            <label>
              초기 입금액 <input name="initialBalance" type="number" defaultValue={0} min={0} />
            </label>
            <button>개설</button>
          </form>
        </div>

        <div className="card">
          <h3>💰 입금</h3>
          <form
            onSubmit={(e) =>
              submit(e, async (d) => {
                await api.deposit(str(d, 'no'), num(d, 'amount'))
                return `${str(d, 'no')} 계좌에 입금 완료.`
              })
            }
          >
            <label>
              계좌번호 <input name="no" placeholder="110-0001" required />
            </label>
            <label>
              입금액 <input name="amount" type="number" defaultValue={10000} min={1} required />
            </label>
            <button>입금</button>
          </form>
        </div>

        <div className="card">
          <h3>💸 출금</h3>
          <form
            onSubmit={(e) =>
              submit(e, async (d) => {
                await api.withdraw(str(d, 'no'), num(d, 'amount'))
                return `${str(d, 'no')} 계좌에서 출금 완료.`
              })
            }
          >
            <label>
              계좌번호 <input name="no" placeholder="110-0001" required />
            </label>
            <label>
              출금액 <input name="amount" type="number" defaultValue={10000} min={1} required />
            </label>
            <button>출금</button>
          </form>
        </div>

        <div className="card">
          <h3>🔄 이체</h3>
          <form
            onSubmit={(e) =>
              submit(e, async (d) => {
                await api.transfer(str(d, 'from'), str(d, 'to'), num(d, 'amount'))
                return `${str(d, 'from')} → ${str(d, 'to')} 이체 완료.`
              })
            }
          >
            <label>
              보내는 계좌 <input name="from" placeholder="110-0001" required />
            </label>
            <label>
              받는 계좌 <input name="to" placeholder="110-0002" required />
            </label>
            <label>
              이체 금액 <input name="amount" type="number" defaultValue={10000} min={1} required />
            </label>
            <button>이체</button>
          </form>
        </div>
      </div>

      <p className="foot">
        프론트(React+TS, :5173)가 REST API로 백엔드(Spring Boot, :8080)를 호출합니다.
      </p>
    </div>
  )
}
