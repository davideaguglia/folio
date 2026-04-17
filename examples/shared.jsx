// shared.jsx — mock data, device shell, tiny icon set

// ─── Mock data ────────────────────────────────────────────────
const MOCK = {
  user: { name: "Sam" },
  month: "April 2026",
  // EUR per app
  netWorth: 48284.55,
  netWorthDelta: 1847.12,
  netWorthDeltaPct: 3.97,
  monthIncome: 4820.00,
  monthExpense: 2196.42,
  budgetTotal: 2600,
  budgetSpent: 2196.42,

  // 30-day spend sparkline (EUR)
  spark: [84,62,140,48,92,210,74,55,68,132,46,88,210,54,92,48,165,72,94,58,122,86,48,92,210,74,68,142,56,96],

  // 12 months income vs expense
  yearBars: [
    { m:"May", i:4200, e:2800 },
    { m:"Jun", i:4400, e:3100 },
    { m:"Jul", i:4200, e:3650 },
    { m:"Aug", i:5100, e:2900 },
    { m:"Sep", i:4200, e:2450 },
    { m:"Oct", i:4400, e:3200 },
    { m:"Nov", i:4200, e:2800 },
    { m:"Dec", i:5800, e:4100 },
    { m:"Jan", i:4200, e:2950 },
    { m:"Feb", i:4200, e:2600 },
    { m:"Mar", i:4600, e:2380 },
    { m:"Apr", i:4820, e:2196 },
  ],

  // Net worth line (months)
  netWorthLine: [41200, 41800, 42400, 43100, 43900, 44200, 44800, 45600, 46100, 46400, 46437, 48284],

  accounts: [
    { id:1, name:"Checking",    type:"CHECKING",    balance:  3842.18, color:"#4f7cff", icon:"🏦" },
    { id:2, name:"Savings",     type:"SAVINGS",     balance: 21450.00, color:"#2ecc71", icon:"💰" },
    { id:3, name:"Cash Wallet", type:"CASH",        balance:   184.50, color:"#f59e0b", icon:"💵" },
    { id:4, name:"Visa Credit", type:"CREDIT_CARD", balance:  -642.13, color:"#ef4444", icon:"💳" },
  ],

  categories: [
    { name:"Groceries",   spent: 412.80, budget: 500, color:"#c8e85a", icon:"🛒" },
    { name:"Rent",        spent: 950.00, budget: 950, color:"#4f7cff", icon:"🏠" },
    { name:"Transport",   spent: 168.40, budget: 220, color:"#f59e0b", icon:"🚊" },
    { name:"Dining",      spent: 247.10, budget: 200, color:"#ef4444", icon:"🍝" },
    { name:"Utilities",   spent: 142.30, budget: 180, color:"#8b5cf6", icon:"⚡" },
    { name:"Subscriptions", spent:  58.40, budget: 80,  color:"#06b6d4", icon:"📺" },
    { name:"Shopping",    spent: 217.42, budget: 300, color:"#ec4899", icon:"🛍" },
    { name:"Other",       spent:   0.00, budget: 170, color:"#94a3b8", icon:"·" },
  ],

  transactions: [
    { id:1,  date:"Today",      time:"08:14", name:"Espresso Bar Toni", cat:"Dining",      acct:"Checking", amt: -4.20,  type:"exp" },
    { id:2,  date:"Today",      time:"07:32", name:"Metro — Monthly",   cat:"Transport",   acct:"Checking", amt: -39.00, type:"exp" },
    { id:3,  date:"Yesterday",  time:"19:22", name:"Trattoria Sole",    cat:"Dining",      acct:"Visa",     amt: -42.80, type:"exp" },
    { id:4,  date:"Yesterday",  time:"17:05", name:"Carrefour",         cat:"Groceries",   acct:"Checking", amt: -63.14, type:"exp" },
    { id:5,  date:"Apr 15",     time:"09:00", name:"Salary — Figma",    cat:"Income",      acct:"Checking", amt: 4820.00,type:"inc" },
    { id:6,  date:"Apr 14",     time:"21:10", name:"Netflix",           cat:"Subscriptions", acct:"Visa",   amt: -14.99, type:"exp", rec:true },
    { id:7,  date:"Apr 14",     time:"14:20", name:"Pharmacy Rossi",    cat:"Other",       acct:"Cash",     amt: -12.40, type:"exp" },
    { id:8,  date:"Apr 13",     time:"11:00", name:"Rent — April",      cat:"Rent",        acct:"Checking", amt: -950.00,type:"exp", rec:true },
    { id:9,  date:"Apr 12",     time:"20:04", name:"Amazon",            cat:"Shopping",    acct:"Visa",     amt: -68.20, type:"exp" },
    { id:10, date:"Apr 11",     time:"08:48", name:"Bakery Millo",      cat:"Groceries",   acct:"Cash",     amt: -6.80,  type:"exp" },
  ],

  investments: [
    { id:1, name:"Vanguard FTSE All-World", tick:"VWCE", type:"ETF",    qty: 48.2,  buy: 102.40, now: 118.72, chg: 15.94 },
    { id:2, name:"Apple Inc.",               tick:"AAPL", type:"STOCK",  qty: 12,    buy: 158.90, now: 212.30, chg: 33.61 },
    { id:3, name:"Bitcoin",                  tick:"BTC",  type:"CRYPTO", qty: 0.082, buy:29400.00,now:62100.00,chg:111.22 },
    { id:4, name:"iShares Core S&P 500",     tick:"CSPX", type:"ETF",    qty: 18,    buy: 412.10, now: 498.40, chg: 20.94 },
    { id:5, name:"Ethereum",                 tick:"ETH",  type:"CRYPTO", qty: 1.4,   buy: 1820.00,now: 3240.00,chg: 78.02 },
  ],

  // Bitcoin 12-pt price history
  btcHistory: [29400, 31200, 35800, 42100, 48200, 51400, 47800, 52600, 58900, 56200, 60400, 62100],
};

// EUR formatter (Italy style → "€ 1.234,56")
function eur(n, { sign=false, compact=false } = {}) {
  const abs = Math.abs(n);
  let body;
  if (compact && abs >= 1000) {
    body = (abs/1000).toLocaleString('it-IT',{minimumFractionDigits:1,maximumFractionDigits:1}) + 'k';
  } else {
    body = abs.toLocaleString('it-IT',{minimumFractionDigits:2,maximumFractionDigits:2});
  }
  const s = n<0 ? '–' : (sign ? '+' : '');
  return `${s}€ ${body}`;
}
function pct(n, { sign=false } = {}) {
  const body = Math.abs(n).toFixed(2) + '%';
  const s = n<0 ? '–' : (sign ? '+' : '');
  return s + body;
}

// ─── Tiny inline icons (stroke-based, 1.5) ────────────────────
const Ico = {
  home:  (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-7h-6v7H4a1 1 0 0 1-1-1z"/></svg>,
  list:  (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" {...p}><path d="M4 6h16M4 12h16M4 18h10"/></svg>,
  card:  (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 10h18M7 15h3"/></svg>,
  chart: (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/></svg>,
  pie:   (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M21 12a9 9 0 1 1-9-9v9z"/><path d="M21 12A9 9 0 0 0 12 3"/></svg>,
  plus:  (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" {...p}><path d="M12 5v14M5 12h14"/></svg>,
  back:  (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M15 18l-6-6 6-6"/></svg>,
  search:(p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/></svg>,
  filter:(p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M3 5h18l-7 9v6l-4-2v-4z"/></svg>,
  more:  (p={}) => <svg viewBox="0 0 24 24" fill="currentColor" {...p}><circle cx="5" cy="12" r="1.6"/><circle cx="12" cy="12" r="1.6"/><circle cx="19" cy="12" r="1.6"/></svg>,
  up:    (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M7 17L17 7M8 7h9v9"/></svg>,
  down:  (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M17 7L7 17M7 8v9h9"/></svg>,
  bell:  (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M6 19h12l-1.5-2V11a4.5 4.5 0 0 0-9 0v6z"/><path d="M10 22h4"/></svg>,
  cog:   (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.9.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.9 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.9l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.9.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.9-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.9V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/></svg>,
  check: (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M4 12l5 5L20 6"/></svg>,
  arrow: (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M5 12h14M13 6l6 6-6 6"/></svg>,
  calendar: (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 10h18M8 3v4M16 3v4"/></svg>,
  tag:   (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M20 12 12 20l-8-8 8-8h8z"/><circle cx="15" cy="9" r="1.4"/></svg>,
  note:  (p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M4 5h16v11l-5 5H4z"/><path d="M7 9h10M7 13h7"/></svg>,
  refresh:(p={}) => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" {...p}><path d="M20 11A8 8 0 1 0 12 20"/><path d="M20 4v7h-7"/></svg>,
};

// ─── Android device shell (minimal, theme-aware) ──────────────
function Device({ bg, fg, statusDark=false, children, width=390, height=820 }) {
  return (
    <div style={{
      width, height, borderRadius: 44, overflow: 'hidden',
      background: bg, color: fg,
      boxShadow: '0 30px 60px rgba(0,0,0,.28), 0 8px 20px rgba(0,0,0,.18), inset 0 0 0 2px rgba(255,255,255,.04)',
      border: '10px solid #111',
      display: 'flex', flexDirection: 'column', boxSizing: 'border-box',
      position: 'relative',
    }}>
      {/* Status bar */}
      <div style={{
        height: 36, display:'flex', alignItems:'center', justifyContent:'space-between',
        padding: '0 22px 0 24px', fontSize: 13, fontWeight: 600,
        color: statusDark ? '#111' : fg, position:'relative', flexShrink:0,
        fontVariantNumeric: 'tabular-nums',
      }}>
        <span>9:41</span>
        <div style={{
          position:'absolute', left:'50%', top:8, transform:'translateX(-50%)',
          width: 18, height: 18, borderRadius: 100, background:'#0a0a0a',
          boxShadow: 'inset 0 0 0 1px rgba(255,255,255,.08)',
        }}/>
        <div style={{display:'flex', gap:6, alignItems:'center'}}>
          <svg width="14" height="10" viewBox="0 0 14 10" fill="currentColor"><path d="M1 7h2v2H1zM4 5h2v4H4zM7 3h2v6H7zM10 1h2v8h-2z"/></svg>
          <svg width="14" height="10" viewBox="0 0 14 10" fill="currentColor"><path d="M7 8.5 1.5 3.8a7 7 0 0 1 11 0L7 8.5z"/></svg>
          <svg width="22" height="10" viewBox="0 0 22 10" fill="none" stroke="currentColor" strokeWidth="1"><rect x=".5" y="1.5" width="18" height="7" rx="1.5"/><rect x="2" y="3" width="14" height="4" fill="currentColor"/><rect x="19.5" y="3.5" width="1.5" height="3" fill="currentColor"/></svg>
        </div>
      </div>
      <div style={{ flex:1, minHeight:0, overflow:'hidden', display:'flex', flexDirection:'column' }}>
        {children}
      </div>
      {/* Gesture pill */}
      <div style={{ height: 22, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
        <div style={{ width: 128, height: 4, borderRadius: 2, background: fg, opacity:.35 }}/>
      </div>
    </div>
  );
}

Object.assign(window, { MOCK, eur, pct, Ico, Device });
