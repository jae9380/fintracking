// ================================================================
// DUMMY DATA — 백엔드 연동 전 개발용 목업 데이터
// 제거 방법: api.ts의 MOCK_MODE = false 설정 후 이 파일 삭제
// ================================================================

// ── DUMMY: 로그인 계정 ────────────────────────────────────────
const DUMMY_USER = { email: "test@test.com", password: "test1234" };

// ── DUMMY: 더미 계좌 정보 ─────────────────────────────────────
let DUMMY_ACCOUNTS = [
  {
    id: 1,
    accountName: "신한 주거래 통장",
    accountNumber: "110-123-456789",
    balance: 2456789,
    accountType: "CHECKING",
  },
  {
    id: 2,
    accountName: "카카오뱅크 적금",
    accountNumber: "333-01-789012",
    balance: 1200000,
    accountType: "SAVINGS",
  },
  {
    id: 3,
    accountName: "미래에셋 투자계좌",
    accountNumber: "240-56-345678",
    balance: 5830000,
    accountType: "INVESTMENT",
  },
];
let accountIdSeq = 4;

// ── DUMMY: 더미 카테고리 ───────────────────────────────────────
let DUMMY_CATEGORIES = [
  { id: 1, name: "급여", type: "INCOME", isDefault: true },
  { id: 2, name: "부업", type: "INCOME", isDefault: false },
  { id: 3, name: "식비", type: "EXPENSE", isDefault: true },
  { id: 4, name: "교통", type: "EXPENSE", isDefault: true },
  { id: 5, name: "의료", type: "EXPENSE", isDefault: false },
  { id: 6, name: "문화", type: "EXPENSE", isDefault: false },
  { id: 7, name: "쇼핑", type: "EXPENSE", isDefault: false },
];
let categoryIdSeq = 8;

// ── DUMMY: 더미 거래 내역 ─────────────────────────────────────
interface DummyTransaction {
  id: number;
  type: string;
  amount: number;
  description: string;
  categoryId: number | null;
  accountId: number;
  toAccountId: number | null;
  transactionDate: string;
}
let DUMMY_TRANSACTIONS: DummyTransaction[] = [
  {
    id: 1,
    type: "INCOME",
    amount: 3200000,
    description: "4월 급여",
    categoryId: 1,
    accountId: 1,
    toAccountId: null,
    transactionDate: "2026-04-25",
  },
  {
    id: 2,
    type: "EXPENSE",
    amount: 68000,
    description: "마트 장보기",
    categoryId: 3,
    accountId: 1,
    toAccountId: null,
    transactionDate: "2026-04-24",
  },
  {
    id: 3,
    type: "EXPENSE",
    amount: 12500,
    description: "지하철 정기권",
    categoryId: 4,
    accountId: 1,
    toAccountId: null,
    transactionDate: "2026-04-23",
  },
  {
    id: 4,
    type: "TRANSFER",
    amount: 300000,
    description: "적금 이체",
    categoryId: null,
    accountId: 1,
    toAccountId: 2,
    transactionDate: "2026-04-22",
  },
  {
    id: 5,
    type: "EXPENSE",
    amount: 45000,
    description: "병원비",
    categoryId: 5,
    accountId: 1,
    toAccountId: null,
    transactionDate: "2026-04-20",
  },
  {
    id: 6,
    type: "INCOME",
    amount: 150000,
    description: "프리랜서 수입",
    categoryId: 2,
    accountId: 1,
    toAccountId: null,
    transactionDate: "2026-04-18",
  },
  {
    id: 7,
    type: "EXPENSE",
    amount: 98000,
    description: "넷플릭스·음악",
    categoryId: 6,
    accountId: 1,
    toAccountId: null,
    transactionDate: "2026-04-15",
  },
  {
    id: 8,
    type: "EXPENSE",
    amount: 32000,
    description: "점심 식사",
    categoryId: 3,
    accountId: 1,
    toAccountId: null,
    transactionDate: "2026-04-14",
  },
  {
    id: 9,
    type: "EXPENSE",
    amount: 124000,
    description: "의류 쇼핑",
    categoryId: 7,
    accountId: 1,
    toAccountId: null,
    transactionDate: "2026-04-12",
  },
  {
    id: 10,
    type: "EXPENSE",
    amount: 18700,
    description: "커피·간식",
    categoryId: 3,
    accountId: 1,
    toAccountId: null,
    transactionDate: "2026-04-10",
  },
  {
    id: 11,
    type: "TRANSFER",
    amount: 500000,
    description: "투자 이체",
    categoryId: null,
    accountId: 1,
    toAccountId: 3,
    transactionDate: "2026-04-05",
  },
  {
    id: 12,
    type: "EXPENSE",
    amount: 19400,
    description: "교통카드 충전",
    categoryId: 4,
    accountId: 1,
    toAccountId: null,
    transactionDate: "2026-04-03",
  },
];
let transactionIdSeq = 13;

// ── DUMMY: 더미 예산 ──────────────────────────────────────────
let DUMMY_BUDGETS = [
  {
    id: 1,
    categoryId: 3,
    amount: 500000,
    spentAmount: 118700,
    yearMonth: "2026-04",
  }, // 식비 23%
  {
    id: 2,
    categoryId: 4,
    amount: 100000,
    spentAmount: 32100,
    yearMonth: "2026-04",
  }, // 교통 32%
  {
    id: 3,
    categoryId: 5,
    amount: 200000,
    spentAmount: 45000,
    yearMonth: "2026-04",
  }, // 의료 22%
  {
    id: 4,
    categoryId: 6,
    amount: 80000,
    spentAmount: 98000,
    yearMonth: "2026-04",
  }, // 문화 122% 초과
];
let budgetIdSeq = 5;

// ── DUMMY: 더미 알림 ─────────────────────────────────────────
let DUMMY_NOTIFICATIONS = [
  {
    id: 1,
    type: "BUDGET_EXCEEDED",
    title: "[2026-04] 문화 예산 초과!",
    message:
      "문화 카테고리 예산 현황\n예산 한도  : 80,000원\n현재 사용  : 98,000원 (122%)\n초과 금액  : 18,000원",
    read: false,
    sentAt: "2026-04-15T14:23:00",
  },
  {
    id: 2,
    type: "BUDGET_WARNING",
    title: "[2026-04] 교통 예산 80% 사용",
    message:
      "교통 카테고리 예산 현황\n예산 한도  : 100,000원\n현재 사용  : 82,000원 (82%)\n남은 예산  : 18,000원",
    read: false,
    sentAt: "2026-04-23T09:11:00",
  },
  {
    id: 3,
    type: "BUDGET_WARNING",
    title: "[2026-03] 식비 예산 50% 사용",
    message:
      "식비 카테고리 예산 현황\n예산 한도  : 500,000원\n현재 사용  : 252,000원 (50%)\n남은 예산  : 248,000원",
    read: true,
    sentAt: "2026-03-20T18:45:00",
  },
  {
    id: 4,
    type: "MONTHLY_REPORT",
    title: "3월 월간 리포트가 생성됐습니다",
    message: "2026년 3월 수입: 3,350,000원\n지출: 1,240,000원\n저축률: 63%",
    read: true,
    sentAt: "2026-04-01T08:00:00",
  },
  {
    id: 5,
    type: "BUDGET_WARNING",
    title: "[2026-04] 의류 예산 80% 사용",
    message:
      "쇼핑 카테고리 예산 현황\n예산 한도  : 150,000원\n현재 사용  : 124,000원 (82%)\n남은 예산  : 26,000원",
    read: false,
    sentAt: "2026-04-12T16:30:00",
  },
  {
    id: 6,
    type: "MONTHLY_REPORT",
    title: "2월 월간 리포트가 생성됐습니다",
    message: "2026년 2월 수입: 3,200,000원\n지출: 980,000원\n저축률: 69%",
    read: true,
    sentAt: "2026-03-01T08:00:00",
  },
];

// ── DUMMY: 더미 유저 토큰 ─────────────────────────────────────
const DUMMY_ACCESS_TOKEN = "dummy-access-token-for-dev";
const DUMMY_REFRESH_TOKEN = "dummy-refresh-token-for-dev";

// ── DUMMY: 응답 헬퍼 ─────────────────────────────────────────
function ok(data: unknown) {
  return { statusCode: 200, message: "OK", data };
}
function created(data: unknown) {
  return { statusCode: 201, message: "Created", data };
}
function noContent() {
  return { statusCode: 204, message: "No Content", data: null };
}
function notFound() {
  return { statusCode: 404, message: "Not found", data: null };
}

// ── DUMMY: 경로 매처 ──────────────────────────────────────────
function match(path: string, pattern: string): Record<string, string> | null {
  const pathParts = path.split("?")[0].split("/");
  const patternParts = pattern.split("/");
  if (pathParts.length !== patternParts.length) return null;
  const params: Record<string, string> = {};
  for (let i = 0; i < patternParts.length; i++) {
    if (patternParts[i].startsWith(":")) {
      params[patternParts[i].slice(1)] = pathParts[i];
    } else if (patternParts[i] !== pathParts[i]) {
      return null;
    }
  }
  return params;
}

function getQuery(path: string): URLSearchParams {
  const q = path.split("?")[1] ?? "";
  return new URLSearchParams(q);
}

// ── DUMMY: 핸들러 ─────────────────────────────────────────────
export function handleMock(
  path: string,
  method: string,
  body?: unknown,
): unknown {
  const m = method.toUpperCase();
  let p: Record<string, string> | null;

  // ── Auth ──────────────────────────────────────────────────
  if (path.includes("/auth/login") && m === "POST") {
    const b = body as Record<string, string>;
    if (b.email !== DUMMY_USER.email || b.password !== DUMMY_USER.password)
      return {
        statusCode: 401,
        message: "이메일 또는 비밀번호가 올바르지 않습니다.",
        data: null,
      };
    return ok({
      accessToken: DUMMY_ACCESS_TOKEN,
      refreshToken: DUMMY_REFRESH_TOKEN,
    });
  }

  if (path.includes("/auth/signup") && m === "POST") return created(1);

  if (path.includes("/auth/reissue") && m === "POST")
    return ok(DUMMY_ACCESS_TOKEN);

  if (path.includes("/auth/logout") && m === "POST") return noContent();

  if (path.includes("/auth/oauth2/login") && m === "POST")
    return ok({
      accessToken: DUMMY_ACCESS_TOKEN,
      refreshToken: DUMMY_REFRESH_TOKEN,
    });

  // ── Accounts ──────────────────────────────────────────────
  if (
    path.includes("/accounts") &&
    m === "GET" &&
    !match(path.split("?")[0], "/account-service/api/v1/accounts/:id")
  )
    return ok([...DUMMY_ACCOUNTS]);

  if (
    (p = match(path.split("?")[0], "/account-service/api/v1/accounts/:id")) &&
    m === "GET"
  )
    return ok(DUMMY_ACCOUNTS.find((a) => a.id === Number(p!.id)) ?? null);

  if (path.includes("/accounts") && m === "POST") {
    const b = body as Record<string, unknown>;
    const newAccount = {
      id: accountIdSeq++,
      accountName: String(b.accountName),
      accountNumber: String(b.accountNumber),
      balance: 0,
      accountType: String(b.accountType),
    };
    DUMMY_ACCOUNTS.push(newAccount);
    return created(newAccount);
  }

  if (
    (p = match(path.split("?")[0], "/account-service/api/v1/accounts/:id")) &&
    m === "DELETE"
  ) {
    DUMMY_ACCOUNTS = DUMMY_ACCOUNTS.filter((a) => a.id !== Number(p!.id));
    return noContent();
  }

  // ── Categories ────────────────────────────────────────────
  if (path.includes("/categories") && m === "GET")
    return ok([...DUMMY_CATEGORIES]);

  if (path.includes("/categories") && m === "POST") {
    const b = body as Record<string, unknown>;
    const cat = {
      id: categoryIdSeq++,
      name: String(b.name),
      type: String(b.type),
      isDefault: false,
    };
    DUMMY_CATEGORIES.push(cat);
    return created(cat);
  }

  if (
    (p = match(
      path.split("?")[0],
      "/transaction-service/api/v1/categories/:id",
    )) &&
    m === "DELETE"
  ) {
    DUMMY_CATEGORIES = DUMMY_CATEGORIES.filter((c) => c.id !== Number(p!.id));
    return noContent();
  }

  // ── Transactions ──────────────────────────────────────────
  if (
    path.includes("/transactions") &&
    m === "GET" &&
    !match(path.split("?")[0], "/transaction-service/api/v1/transactions/:id")
  ) {
    const q = getQuery(path);
    const accountId = q.get("accountId");
    const list = accountId
      ? DUMMY_TRANSACTIONS.filter((t) => t.accountId === Number(accountId))
      : [...DUMMY_TRANSACTIONS];
    return ok(
      list.sort((a, b) => b.transactionDate.localeCompare(a.transactionDate)),
    );
  }

  if (
    (p = match(
      path.split("?")[0],
      "/transaction-service/api/v1/transactions/:id",
    )) &&
    m === "GET"
  )
    return ok(DUMMY_TRANSACTIONS.find((t) => t.id === Number(p!.id)) ?? null);

  if (path.includes("/transactions") && m === "POST") {
    const b = body as Record<string, unknown>;
    const t = {
      id: transactionIdSeq++,
      type: String(b.type),
      amount: Number(b.amount),
      description: String(b.description ?? ""),
      categoryId: b.categoryId ? Number(b.categoryId) : null,
      accountId: Number(b.accountId),
      toAccountId: b.toAccountId ? Number(b.toAccountId) : null,
      transactionDate: String(b.transactionDate),
    };
    DUMMY_TRANSACTIONS.unshift(t);
    return created(t);
  }

  if (
    (p = match(
      path.split("?")[0],
      "/transaction-service/api/v1/transactions/:id",
    )) &&
    m === "PATCH"
  ) {
    const b = body as Record<string, unknown>;
    const t = DUMMY_TRANSACTIONS.find((t) => t.id === Number(p!.id));
    if (!t) return notFound();
    if (b.amount) t.amount = Number(b.amount);
    if (b.description) t.description = String(b.description);
    if (b.transactionDate) t.transactionDate = String(b.transactionDate);
    return ok(t);
  }

  if (
    (p = match(
      path.split("?")[0],
      "/transaction-service/api/v1/transactions/:id",
    )) &&
    m === "DELETE"
  ) {
    DUMMY_TRANSACTIONS = DUMMY_TRANSACTIONS.filter(
      (t) => t.id !== Number(p!.id),
    );
    return noContent();
  }

  // ── Budgets ───────────────────────────────────────────────
  if (
    path.includes("/budgets") &&
    m === "GET" &&
    !match(path.split("?")[0], "/budget-service/api/v1/budgets/:id")
  ) {
    const q = getQuery(path);
    const ym = q.get("yearMonth") ?? "2026-04";
    return ok(DUMMY_BUDGETS.filter((b) => b.yearMonth === ym));
  }

  if (
    (p = match(path.split("?")[0], "/budget-service/api/v1/budgets/:id")) &&
    m === "GET"
  )
    return ok(DUMMY_BUDGETS.find((b) => b.id === Number(p!.id)) ?? null);

  if (path.includes("/budgets") && m === "POST") {
    const b = body as Record<string, unknown>;
    const budget = {
      id: budgetIdSeq++,
      categoryId: Number(b.categoryId),
      amount: Number(b.amount),
      spentAmount: 0,
      yearMonth: String(b.yearMonth),
    };
    DUMMY_BUDGETS.push(budget);
    return created(budget);
  }

  if (
    (p = match(path.split("?")[0], "/budget-service/api/v1/budgets/:id")) &&
    m === "PUT"
  ) {
    const b = body as Record<string, unknown>;
    const budget = DUMMY_BUDGETS.find((b) => b.id === Number(p!.id));
    if (!budget) return notFound();
    budget.amount = Number(b.amount);
    return ok(budget);
  }

  if (
    (p = match(path.split("?")[0], "/budget-service/api/v1/budgets/:id")) &&
    m === "DELETE"
  ) {
    DUMMY_BUDGETS = DUMMY_BUDGETS.filter((b) => b.id !== Number(p!.id));
    return noContent();
  }

  // ── Notifications ─────────────────────────────────────────
  if (
    path.includes("/notifications") &&
    m === "GET" &&
    !path.includes("/read")
  ) {
    const q = getQuery(path);
    const readFilter = q.get("read");
    const page = Number(q.get("page") ?? 0);
    const size = Number(q.get("size") ?? 20);
    let list = [...DUMMY_NOTIFICATIONS];
    if (readFilter === "true") list = list.filter((n) => n.read);
    if (readFilter === "false") list = list.filter((n) => !n.read);
    list.sort((a, b) => b.sentAt.localeCompare(a.sentAt));
    const start = page * size;
    const content = list.slice(start, start + size);
    return ok({
      content,
      totalElements: list.length,
      totalPages: Math.ceil(list.length / size),
      last: start + size >= list.length,
      number: page,
    });
  }

  if (path.includes("/read-all") && m === "PATCH") {
    DUMMY_NOTIFICATIONS.forEach((n) => {
      n.read = true;
    });
    return noContent();
  }

  if (
    (p = match(
      path.split("?")[0],
      "/notification-service/api/v1/notifications/:id/read",
    )) &&
    m === "PATCH"
  ) {
    const n = DUMMY_NOTIFICATIONS.find((n) => n.id === Number(p!.id));
    if (n) n.read = true;
    return ok(n ?? null);
  }

  // fallback
  console.warn("[DUMMY] 핸들러 없음:", m, path);
  return ok(null);
}
