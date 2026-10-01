// 백엔드가 JSON으로 내려주는 계좌 데이터의 타입
export interface Account {
  id: number
  accountNumber: string
  owner: string
  balance: number
}
