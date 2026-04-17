// atelier.jsx — Direction B: "Atelier"
// Calm, editorial, warm-cream paper. Serif display numbers, rich forest green
// with terracotta accent. Human, airy — but still dense enough at 7/10.

const A = {
  bg:        '#f4efe6',   // warm cream
  paper:     '#fbf7ef',   // lighter paper
  ink:       '#1f2218',   // near-black w/ warmth
  ink2:      '#4a4d42',
  ink3:      '#8a8c7e',
  ink4:      '#c5c3b6',
  rule:      'rgba(31,34,24,.10)',
  ruleSoft:  'rgba(31,34,24,.06)',
  green:     '#2d5a3f',   // forest
  greenSoft: '#cde0ce',
  terra:     '#c4683a',   // terracotta
  terraSoft: '#f1d7c4',
  gold:      '#b08a3e',
  neg:       '#a94436',
  serif:     "'Fraunces', 'Cormorant Garamond', 'Playfair Display', Georgia, serif",
  sans:      "'Inter', system-ui, -apple-system, sans-serif",
  mono:      "'JetBrains Mono', ui-monospace, monospace",
};

// ─── Primitives ───────────────────────────────────────────────
function AHeader({ eyebrow, title, right, sub }) {
  return (
    <div style={{ padding:'14px 22px 12px', background:A.bg }}>
      <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between' }}>
        <div>
          <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.22em', color:A.ink3, textTransform:'uppercase' }}>
            {eyebrow}
          </div>
          <div style={{ fontFamily:A.serif, fontSize:26, color:A.ink, marginTop:2, letterSpacing:'-.01em', lineHeight:1.1, fontWeight:400 }}>
            {title}
          </div>
          {sub && <div style={{ fontFamily:A.sans, fontSize:12, color:A.ink3, marginTop:2 }}>{sub}</div>}
        </div>
        <div style={{ display:'flex', gap:8 }}>{right}</div>
      </div>
    </div>
  );
}

function AIconBtn({ children, onClick, filled }) {
  return (
    <button onClick={onClick} style={{
      width:38, height:38, borderRadius:100,
      border:`1px solid ${filled?A.green:A.rule}`,
      background: filled ? A.green : A.paper,
      color: filled ? A.paper : A.ink, cursor:'pointer',
      display:'flex', alignItems:'center', justifyContent:'center',
    }}>{children}</button>
  );
}

function ACard({ children, style }) {
  return (
    <div style={{
      background:A.paper, borderRadius:14, border:`1px solid ${A.rule}`,
      overflow:'hidden', ...style,
    }}>{children}</div>
  );
}

function ALine({ data, width=320, height=60, stroke=A.green, fill=true }) {
  const max = Math.max(...data), min = Math.min(...data);
  const rng = max-min || 1;
  const step = width/(data.length-1);
  const pts = data.map((v,i)=>[i*step, height - ((v-min)/rng)*(height-6) - 3]);
  // smooth
  let d = `M ${pts[0][0]} ${pts[0][1]}`;
  for (let i=1;i<pts.length;i++){
    const p0 = pts[i-1], p1 = pts[i];
    const cx = (p0[0]+p1[0])/2;
    d += ` C ${cx} ${p0[1]}, ${cx} ${p1[1]}, ${p1[0]} ${p1[1]}`;
  }
  const area = d + ` L ${width} ${height} L 0 ${height} Z`;
  return (
    <svg width={width} height={height} style={{ display:'block' }}>
      {fill && <path d={area} fill={stroke} opacity=".10"/>}
      <path d={d} fill="none" stroke={stroke} strokeWidth="1.8" strokeLinejoin="round" strokeLinecap="round"/>
      <circle cx={pts[pts.length-1][0]} cy={pts[pts.length-1][1]} r="3" fill={A.paper} stroke={stroke} strokeWidth="1.8"/>
    </svg>
  );
}

function ADonut({ segs, size=110, thick=16 }) {
  const total = segs.reduce((a,s)=>a+s.v,0);
  const r = size/2 - thick/2 - 1;
  const cx = size/2, cy = size/2;
  let a0 = -Math.PI/2;
  return (
    <svg width={size} height={size} style={{ display:'block' }}>
      <circle cx={cx} cy={cy} r={r} fill="none" stroke={A.ruleSoft} strokeWidth={thick}/>
      {segs.map((s,i)=>{
        const a1 = a0 + (s.v/total)*Math.PI*2;
        const gap = 0.015;
        const large = (a1-a0-gap*2) > Math.PI ? 1 : 0;
        const x0 = cx + r*Math.cos(a0+gap), y0 = cy + r*Math.sin(a0+gap);
        const x1 = cx + r*Math.cos(a1-gap), y1 = cy + r*Math.sin(a1-gap);
        const d = `M ${x0} ${y0} A ${r} ${r} 0 ${large} 1 ${x1} ${y1}`;
        const el = <path key={i} d={d} fill="none" stroke={s.c} strokeWidth={thick} strokeLinecap="butt"/>;
        a0 = a1;
        return el;
      })}
    </svg>
  );
}

function ABars({ data, width=320, height=100 }) {
  const max = Math.max(...data.map(d=>Math.max(d.i, d.e)));
  const slot = width / data.length;
  const barW = slot/2 - 4;
  return (
    <svg width={width} height={height} style={{ display:'block' }}>
      {data.map((d,i)=>{
        const x = i*slot + 2;
        const iH = (d.i/max)*(height-18);
        const eH = (d.e/max)*(height-18);
        return (
          <g key={i}>
            <rect x={x} y={height-18-iH} width={barW} height={iH} fill={A.green} opacity=".85" rx="1.5"/>
            <rect x={x+barW+2} y={height-18-eH} width={barW} height={eH} fill={A.terra} opacity=".85" rx="1.5"/>
            <text x={x+barW+1} y={height-4} fontSize="8" fill={A.ink3} textAnchor="middle" fontFamily={A.sans}>{d.m}</text>
          </g>
        );
      })}
    </svg>
  );
}

// ─── Bottom nav ──────────────────────────────────────────────
function ATabBar({ tab, setTab }) {
  const tabs = [
    { k:'home',   i:<Ico.home  width="20"/>, l:'Home' },
    { k:'tx',     i:<Ico.list  width="20"/>, l:'Ledger' },
    { k:'accts',  i:<Ico.card  width="20"/>, l:'Accounts' },
    { k:'invest', i:<Ico.chart width="20"/>, l:'Invest' },
    { k:'budget', i:<Ico.pie   width="20"/>, l:'Budget' },
  ];
  return (
    <div style={{
      display:'flex', background:A.paper, borderTop:`1px solid ${A.rule}`,
      padding:'6px 8px 4px', gap:2,
    }}>
      {tabs.map(t=>{
        const act = tab===t.k;
        return (
          <button key={t.k} onClick={()=>setTab(t.k)} style={{
            flex:1, background:'transparent', border:'none', cursor:'pointer',
            padding:'6px 0 4px', display:'flex', flexDirection:'column', alignItems:'center', gap:3,
            color: act ? A.green : A.ink3,
          }}>
            <div style={{
              padding: act ? '5px 16px' : '5px 8px', borderRadius:100,
              background: act ? A.greenSoft : 'transparent',
              display:'flex', alignItems:'center', justifyContent:'center', transition:'all .2s',
            }}>{t.i}</div>
            <div style={{ fontFamily:A.sans, fontSize:10, fontWeight: act?600:400 }}>{t.l}</div>
          </button>
        );
      })}
    </div>
  );
}

// ═══ HOME ════════════════════════════════════════════════════
function AHome() {
  const spendPct = MOCK.budgetSpent / MOCK.budgetTotal;
  return (
    <div style={{ flex:1, overflow:'auto', background:A.bg }}>
      <AHeader
        eyebrow="Thursday · April 17"
        title="Good morning, Sam"
        sub="A quiet week — you're 17% under budget."
        right={<>
          <AIconBtn><Ico.search width="16"/></AIconBtn>
          <AIconBtn><Ico.bell   width="16"/></AIconBtn>
        </>}
      />

      {/* Net worth hero */}
      <div style={{ padding:'4px 16px 16px' }}>
        <ACard>
          <div style={{ padding:'18px 18px 6px', display:'flex', justifyContent:'space-between', alignItems:'flex-start' }}>
            <div>
              <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.22em', color:A.ink3, textTransform:'uppercase' }}>Net Worth</div>
              <div style={{ fontFamily:A.serif, fontSize:40, color:A.ink, marginTop:4, lineHeight:1, letterSpacing:'-.02em', fontVariantNumeric:'oldstyle-nums', fontWeight:400 }}>
                {eur(MOCK.netWorth)}
              </div>
              <div style={{ marginTop:8, display:'flex', alignItems:'center', gap:8, fontFamily:A.sans, fontSize:12 }}>
                <span style={{ color:A.green, fontWeight:600 }}>↗ {eur(MOCK.netWorthDelta,{sign:true})}</span>
                <span style={{ color:A.green, background:A.greenSoft, padding:'2px 8px', borderRadius:100, fontWeight:600 }}>
                  {pct(MOCK.netWorthDeltaPct, {sign:true})}
                </span>
                <span style={{ color:A.ink3 }}>this month</span>
              </div>
            </div>
            <div style={{ textAlign:'right' }}>
              <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.18em', color:A.ink3, textTransform:'uppercase' }}>12-MO</div>
            </div>
          </div>
          <div style={{ padding:'0 10px 10px' }}>
            <ALine data={MOCK.netWorthLine} width={326} height={68}/>
            <div style={{ display:'flex', justifyContent:'space-between', padding:'0 8px', fontFamily:A.sans, fontSize:9, color:A.ink4, letterSpacing:'.06em' }}>
              {['May','Jul','Sep','Nov','Jan','Mar','Apr'].map(m=><span key={m}>{m}</span>)}
            </div>
          </div>
        </ACard>
      </div>

      {/* Income / expense duo */}
      <div style={{ padding:'0 16px 16px', display:'grid', gridTemplateColumns:'1fr 1fr', gap:10 }}>
        <ACard style={{ padding:14 }}>
          <div style={{ display:'flex', alignItems:'center', gap:6, color:A.green }}>
            <div style={{ width:6, height:6, borderRadius:100, background:A.green }}/>
            <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.18em', textTransform:'uppercase', fontWeight:600 }}>Earned</div>
          </div>
          <div style={{ fontFamily:A.serif, fontSize:22, color:A.ink, marginTop:6, letterSpacing:'-.01em' }}>
            {eur(MOCK.monthIncome, {compact:true})}
          </div>
          <div style={{ fontFamily:A.sans, fontSize:11, color:A.ink3, marginTop:2 }}>+12.4% vs Mar</div>
        </ACard>
        <ACard style={{ padding:14 }}>
          <div style={{ display:'flex', alignItems:'center', gap:6, color:A.terra }}>
            <div style={{ width:6, height:6, borderRadius:100, background:A.terra }}/>
            <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.18em', textTransform:'uppercase', fontWeight:600 }}>Spent</div>
          </div>
          <div style={{ fontFamily:A.serif, fontSize:22, color:A.ink, marginTop:6, letterSpacing:'-.01em' }}>
            {eur(MOCK.monthExpense, {compact:true})}
          </div>
          <div style={{ fontFamily:A.sans, fontSize:11, color:A.ink3, marginTop:2 }}>–8.1% vs Mar</div>
        </ACard>
      </div>

      {/* Budget ring + categories */}
      <div style={{ padding:'0 16px 16px' }}>
        <ACard>
          <div style={{ padding:'16px 18px 14px', display:'flex', gap:16, alignItems:'center' }}>
            <div style={{ position:'relative', flexShrink:0 }}>
              <ADonut size={92} thick={12} segs={MOCK.categories.filter(c=>c.spent>0).map(c=>({v:c.spent,c:c.color}))}/>
              <div style={{ position:'absolute', inset:0, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center' }}>
                <div style={{ fontFamily:A.serif, fontSize:19, color:A.ink, lineHeight:1 }}>{(spendPct*100).toFixed(0)}%</div>
                <div style={{ fontFamily:A.sans, fontSize:9, color:A.ink3, letterSpacing:'.1em', textTransform:'uppercase' }}>used</div>
              </div>
            </div>
            <div style={{ flex:1 }}>
              <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.22em', color:A.ink3, textTransform:'uppercase' }}>April Budget</div>
              <div style={{ fontFamily:A.serif, fontSize:22, color:A.ink, marginTop:3, letterSpacing:'-.01em' }}>
                {eur(MOCK.budgetTotal - MOCK.budgetSpent, {compact:true})} <span style={{ fontFamily:A.sans, fontSize:13, color:A.ink3 }}>left</span>
              </div>
              <div style={{ fontFamily:A.sans, fontSize:11, color:A.ink3, marginTop:2 }}>
                of {eur(MOCK.budgetTotal, {compact:true})} · 13 days to go
              </div>
            </div>
          </div>
          {/* Top 3 categories inline */}
          <div style={{ borderTop:`1px solid ${A.ruleSoft}`, padding:'4px 6px' }}>
            {MOCK.categories.slice(0,3).map((c,i)=>{
              const p = Math.min(c.spent/c.budget, 1);
              return (
                <div key={i} style={{ padding:'8px 12px', display:'flex', alignItems:'center', gap:10 }}>
                  <span style={{ width:8, height:8, borderRadius:100, background:c.color }}/>
                  <span style={{ fontFamily:A.sans, fontSize:13, color:A.ink, flex:1 }}>{c.name}</span>
                  <div style={{ flex:1.5, height:4, background:A.ruleSoft, borderRadius:100, overflow:'hidden' }}>
                    <div style={{ width:`${p*100}%`, height:'100%', background:c.color, borderRadius:100 }}/>
                  </div>
                  <span style={{ fontFamily:A.mono, fontSize:11, color:A.ink2, minWidth:72, textAlign:'right' }}>
                    {eur(c.spent,{compact:true}).replace('€ ','€')}<span style={{color:A.ink4}}>/{eur(c.budget,{compact:true}).replace('€ ','')}</span>
                  </span>
                </div>
              );
            })}
          </div>
        </ACard>
      </div>

      {/* Recent transactions */}
      <div style={{ padding:'0 16px 90px' }}>
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'baseline', padding:'6px 6px 10px' }}>
          <div style={{ fontFamily:A.serif, fontSize:17, color:A.ink, letterSpacing:'-.01em' }}>Recent activity</div>
          <div style={{ fontFamily:A.sans, fontSize:12, color:A.green, fontWeight:600 }}>See all →</div>
        </div>
        <ACard>
          {MOCK.transactions.slice(0,5).map((t,i)=>(
            <ATxRow key={t.id} t={t} last={i===4}/>
          ))}
        </ACard>
      </div>
    </div>
  );
}

function ATxRow({ t, last, wide }) {
  const neg = t.amt < 0;
  return (
    <div style={{
      display:'flex', alignItems:'center', gap:12, padding:'12px 14px',
      borderBottom: last?'none':`1px solid ${A.ruleSoft}`,
    }}>
      <div style={{
        width:38, height:38, borderRadius:100, flexShrink:0,
        background: neg ? A.terraSoft : A.greenSoft,
        display:'flex', alignItems:'center', justifyContent:'center',
        fontSize:16,
      }}>{t.cat==='Income' ? '✦' : t.cat==='Rent' ? '⌂' : t.cat==='Dining' ? '◐' : t.cat==='Groceries' ? '◆' : t.cat==='Transport' ? '▷' : t.cat==='Subscriptions' ? '◇' : '·'}</div>
      <div style={{ flex:1, minWidth:0 }}>
        <div style={{ fontFamily:A.sans, fontSize:14, color:A.ink, whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis' }}>
          {t.name} {t.rec && <span style={{ color:A.ink3, fontSize:10 }}>↻</span>}
        </div>
        <div style={{ fontFamily:A.sans, fontSize:11.5, color:A.ink3, marginTop:1 }}>
          {t.cat} · {t.acct}{wide ? ` · ${t.time}` : ''}
        </div>
      </div>
      <div style={{ textAlign:'right' }}>
        <div style={{ fontFamily:A.mono, fontSize:14, color: neg?A.ink:A.green, fontWeight:500 }}>
          {neg?'–':'+'}{eur(Math.abs(t.amt)).replace('€ ','€')}
        </div>
        <div style={{ fontFamily:A.sans, fontSize:10, color:A.ink4, marginTop:1 }}>{t.date}</div>
      </div>
    </div>
  );
}

// ═══ TRANSACTIONS ════════════════════════════════════════════
function ATransactions() {
  const groups = {};
  MOCK.transactions.forEach(t => { (groups[t.date] = groups[t.date]||[]).push(t); });
  return (
    <div style={{ flex:1, overflow:'auto', background:A.bg, position:'relative' }}>
      <AHeader
        eyebrow={`${MOCK.transactions.length} entries · April`}
        title="The Ledger"
        right={<>
          <AIconBtn><Ico.search width="16"/></AIconBtn>
          <AIconBtn><Ico.filter width="16"/></AIconBtn>
        </>}
      />

      <div style={{ display:'flex', gap:6, padding:'4px 22px 14px', overflowX:'auto' }}>
        {['All','Expenses','Income','Recurring','This month'].map((c,i)=>(
          <div key={c} style={{
            fontFamily:A.sans, fontSize:12, padding:'6px 14px', borderRadius:100,
            background: i===0 ? A.ink : A.paper,
            color: i===0 ? A.paper : A.ink2,
            border: i===0 ? 'none' : `1px solid ${A.rule}`,
            flexShrink:0, fontWeight: i===0?600:400,
          }}>{c}</div>
        ))}
      </div>

      {/* Summary strip */}
      <div style={{ margin:'0 16px 14px', padding:'12px 16px', background:A.paper, borderRadius:12, border:`1px solid ${A.rule}`,
        display:'flex', justifyContent:'space-between', alignItems:'center' }}>
        <div>
          <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.2em', color:A.ink3, textTransform:'uppercase' }}>Net flow · April</div>
          <div style={{ fontFamily:A.serif, fontSize:22, color:A.green, marginTop:2 }}>
            +{eur(MOCK.monthIncome - MOCK.monthExpense).replace('€ ','€')}
          </div>
        </div>
        <div style={{ textAlign:'right', fontFamily:A.sans, fontSize:11, color:A.ink3, lineHeight:1.5 }}>
          <div>In <span style={{color:A.green, fontFamily:A.mono}}>{eur(MOCK.monthIncome,{compact:true}).replace('€ ','€')}</span></div>
          <div>Out <span style={{color:A.terra, fontFamily:A.mono}}>{eur(MOCK.monthExpense,{compact:true}).replace('€ ','€')}</span></div>
        </div>
      </div>

      {Object.entries(groups).map(([date, items])=>(
        <div key={date} style={{ padding:'0 16px 12px' }}>
          <div style={{ fontFamily:A.sans, fontSize:11, color:A.ink3, letterSpacing:'.14em', textTransform:'uppercase', padding:'4px 6px 8px', display:'flex', justifyContent:'space-between' }}>
            <span>{date}</span>
            <span style={{ fontFamily:A.mono, color:A.ink4 }}>
              {items.reduce((a,t)=>a+t.amt,0) > 0 ? '+':''}{eur(items.reduce((a,t)=>a+t.amt,0)).replace('€ ','€')}
            </span>
          </div>
          <ACard>
            {items.map((t,i)=>(
              <ATxRow key={t.id} t={t} wide last={i===items.length-1}/>
            ))}
          </ACard>
        </div>
      ))}
      <div style={{ height:90 }}/>

      {/* FAB */}
      <button style={{
        position:'absolute', right:20, bottom:20,
        width:56, height:56, borderRadius:100, border:'none',
        background:A.green, color:A.paper, cursor:'pointer',
        display:'flex', alignItems:'center', justifyContent:'center',
        boxShadow:'0 12px 30px rgba(45,90,63,.35)',
      }}><Ico.plus width="24"/></button>
    </div>
  );
}

// ═══ ADD ═════════════════════════════════════════════════════
function AAdd() {
  const [type, setType] = React.useState('expense');
  return (
    <div style={{ flex:1, overflow:'auto', background:A.bg, display:'flex', flexDirection:'column' }}>
      <AHeader eyebrow="April 17 · Thursday" title="New entry"
        right={<AIconBtn><Ico.check width="16"/></AIconBtn>}
      />

      {/* Segmented */}
      <div style={{ margin:'0 16px 18px', background:A.paper, padding:4, borderRadius:100, border:`1px solid ${A.rule}`, display:'flex' }}>
        {['expense','income','transfer'].map(t=>(
          <button key={t} onClick={()=>setType(t)} style={{
            flex:1, padding:'9px 0', borderRadius:100, border:'none', cursor:'pointer',
            background: type===t ? (t==='income'?A.green:t==='expense'?A.terra:A.ink) : 'transparent',
            color: type===t ? A.paper : A.ink2,
            fontFamily:A.sans, fontSize:13, fontWeight: type===t?600:500,
            textTransform:'capitalize',
          }}>{t}</button>
        ))}
      </div>

      {/* Amount */}
      <div style={{ padding:'20px 22px 26px', textAlign:'center' }}>
        <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.22em', color:A.ink3, textTransform:'uppercase' }}>Amount</div>
        <div style={{ marginTop:10, fontFamily:A.serif, fontSize:72, color: type==='income'?A.green:A.ink, lineHeight:1, letterSpacing:'-.03em', fontVariantNumeric:'oldstyle-nums' }}>
          <span style={{ fontSize:36, color:A.ink3, marginRight:4, verticalAlign:'top' }}>€</span>42,80
        </div>
        <div style={{ marginTop:14, display:'flex', justifyContent:'center', gap:6, flexWrap:'wrap' }}>
          {['€5','€10','€20','€50','€100'].map(q=>(
            <button key={q} style={{
              fontFamily:A.sans, fontSize:11, padding:'5px 12px', borderRadius:100,
              background:A.paper, color:A.ink2, border:`1px solid ${A.rule}`, cursor:'pointer',
            }}>{q}</button>
          ))}
        </div>
      </div>

      {/* Fields */}
      <div style={{ padding:'0 16px 16px' }}>
        <ACard>
          <AFieldRow label="Category" value="Dining"          right={<span style={{ width:10, height:10, borderRadius:100, background:'#ef4444', marginRight:6 }}/>}/>
          <AFieldRow label="Account"  value="Visa Credit"     right={<span style={{ width:10, height:10, borderRadius:100, background:'#4f7cff', marginRight:6 }}/>}/>
          <AFieldRow label="Date"     value="Thu, Apr 17 · 13:24"/>
          <AFieldRow label="Note"     value="Trattoria Sole — dinner w/ L." long/>
          <AFieldRow label="Recurring" value="Does not repeat" muted last/>
        </ACard>
      </div>

      <div style={{ flex:1 }}/>

      <div style={{ padding:'14px 16px 16px', display:'flex', gap:10 }}>
        <button style={{
          flex:1, padding:'15px 0', background:A.paper, border:`1px solid ${A.rule}`,
          borderRadius:100, color:A.ink2, fontFamily:A.sans, fontSize:14, fontWeight:500, cursor:'pointer',
        }}>Cancel</button>
        <button style={{
          flex:2, padding:'15px 0', background:A.green, border:'none',
          borderRadius:100, color:A.paper, fontFamily:A.sans, fontSize:14, fontWeight:600, cursor:'pointer',
        }}>Save entry</button>
      </div>
    </div>
  );
}

function AFieldRow({ label, value, muted, right, last, long }) {
  return (
    <div style={{
      display:'flex', alignItems:'center', gap:12, padding:'14px 16px',
      borderBottom: last?'none':`1px solid ${A.ruleSoft}`,
    }}>
      <div style={{ flex:1, minWidth:0 }}>
        <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.2em', color:A.ink3, textTransform:'uppercase' }}>{label}</div>
        <div style={{ display:'flex', alignItems:'center', marginTop:3, overflow:'hidden' }}>
          {right}
          <div style={{ fontFamily: long?A.sans:A.sans, fontSize:14, color: muted?A.ink3:A.ink, fontStyle: long?'italic':'normal', whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis' }}>{value}</div>
        </div>
      </div>
      <Ico.arrow width="16" style={{ color:A.ink3 }}/>
    </div>
  );
}

// ═══ ACCOUNTS ════════════════════════════════════════════════
function AAccounts() {
  const total = MOCK.accounts.reduce((a,x)=>a+x.balance, 0);
  return (
    <div style={{ flex:1, overflow:'auto', background:A.bg }}>
      <AHeader
        eyebrow={`${MOCK.accounts.length} accounts · all cash`}
        title="Accounts"
        right={<AIconBtn filled><Ico.plus width="16"/></AIconBtn>}
      />

      {/* Hero total */}
      <div style={{ padding:'0 22px 18px' }}>
        <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.22em', color:A.ink3, textTransform:'uppercase' }}>Liquid position</div>
        <div style={{ fontFamily:A.serif, fontSize:44, color:A.ink, marginTop:4, letterSpacing:'-.02em', lineHeight:1 }}>
          {eur(total)}
        </div>
        <div style={{ fontFamily:A.sans, fontSize:12, color:A.green, marginTop:6, fontWeight:600 }}>
          ↗ +{eur(523.18)} in the last 30 days
        </div>
      </div>

      {/* Account cards — stack of paper */}
      <div style={{ padding:'0 16px 14px', display:'flex', flexDirection:'column', gap:10 }}>
        {MOCK.accounts.map(a=>(
          <ACard key={a.id}>
            <div style={{ padding:'16px 16px 12px', display:'flex', alignItems:'center', gap:14 }}>
              <div style={{
                width:48, height:48, borderRadius:12, background:a.color,
                display:'flex', alignItems:'center', justifyContent:'center', fontSize:22,
                flexShrink:0,
              }}>{a.icon}</div>
              <div style={{ flex:1, minWidth:0 }}>
                <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.2em', color:A.ink3, textTransform:'uppercase' }}>
                  {a.type.replace('_',' ')}
                </div>
                <div style={{ fontFamily:A.serif, fontSize:18, color:A.ink, marginTop:2, letterSpacing:'-.01em' }}>{a.name}</div>
              </div>
              <div style={{ textAlign:'right' }}>
                <div style={{ fontFamily:A.serif, fontSize:20, color: a.balance<0?A.neg:A.ink, letterSpacing:'-.01em' }}>
                  {eur(a.balance)}
                </div>
                <div style={{ fontFamily:A.sans, fontSize:11, color: a.balance<0?A.neg:A.green, marginTop:1 }}>
                  {a.balance<0 ? '▼ payment due' : '▲ +2.1%'}
                </div>
              </div>
            </div>
            <div style={{ padding:'0 8px 10px' }}>
              <ALine data={
                a.id===1 ? [3200,3150,3420,3380,3510,3680,3640,3720,3800,3842]
              : a.id===2 ? [21000,21050,21150,21200,21280,21320,21380,21400,21430,21450]
              : a.id===3 ? [240,220,210,200,195,210,200,190,185,184]
              :            [-420,-480,-520,-560,-600,-620,-635,-640,-642,-642]
              } width={334} height={34} stroke={a.balance<0?A.neg:A.green} fill={false}/>
            </div>
          </ACard>
        ))}
      </div>
      <div style={{ height:60 }}/>
    </div>
  );
}

// ═══ INVESTMENTS ═════════════════════════════════════════════
function AInvest() {
  const totalValue = MOCK.investments.reduce((a,i)=>a + i.qty*i.now, 0);
  const totalCost  = MOCK.investments.reduce((a,i)=>a + i.qty*i.buy, 0);
  const totalChg   = ((totalValue/totalCost - 1) * 100);

  return (
    <div style={{ flex:1, overflow:'auto', background:A.bg }}>
      <AHeader
        eyebrow="Last priced 09:40"
        title="Portfolio"
        right={<>
          <AIconBtn><Ico.refresh width="16"/></AIconBtn>
          <AIconBtn filled><Ico.plus width="16"/></AIconBtn>
        </>}
      />

      {/* Hero + curve */}
      <div style={{ padding:'0 16px 14px' }}>
        <ACard>
          <div style={{ padding:'16px 18px 6px', display:'flex', justifyContent:'space-between', alignItems:'flex-start' }}>
            <div>
              <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.22em', color:A.ink3, textTransform:'uppercase' }}>Total value</div>
              <div style={{ fontFamily:A.serif, fontSize:36, color:A.ink, marginTop:3, letterSpacing:'-.02em', lineHeight:1 }}>
                {eur(totalValue)}
              </div>
              <div style={{ display:'flex', gap:8, marginTop:8, fontFamily:A.sans, fontSize:12 }}>
                <span style={{ color:A.green, fontWeight:600 }}>↗ {eur(totalValue-totalCost, {sign:true, compact:true})}</span>
                <span style={{ color:A.green, background:A.greenSoft, padding:'2px 8px', borderRadius:100, fontWeight:600 }}>+{totalChg.toFixed(1)}%</span>
              </div>
            </div>
            <div style={{ display:'flex', gap:2 }}>
              {['1M','3M','1Y','ALL'].map((r,i)=>(
                <div key={r} style={{
                  fontFamily:A.sans, fontSize:10, padding:'4px 8px', borderRadius:100,
                  background: i===3 ? A.ink : 'transparent',
                  color: i===3 ? A.paper : A.ink3, fontWeight:600,
                }}>{r}</div>
              ))}
            </div>
          </div>
          <div style={{ padding:'0 6px 10px' }}>
            <ALine data={MOCK.btcHistory.map((v,i)=>totalValue*0.7 + (v/62100)*totalValue*0.3)} width={338} height={80}/>
          </div>
        </ACard>
      </div>

      {/* Holdings */}
      <div style={{ padding:'0 16px 90px' }}>
        <div style={{ fontFamily:A.sans, fontSize:11, letterSpacing:'.14em', color:A.ink3, textTransform:'uppercase', padding:'4px 6px 10px' }}>Holdings</div>
        <ACard>
          {MOCK.investments.map((i, idx)=>{
            const val = i.qty * i.now;
            const pos = i.chg >= 0;
            const last = idx === MOCK.investments.length-1;
            return (
              <div key={i.id} style={{
                display:'flex', alignItems:'center', gap:12, padding:'14px 14px',
                borderBottom: last?'none':`1px solid ${A.ruleSoft}`,
              }}>
                <div style={{
                  width:40, height:40, borderRadius:10, flexShrink:0,
                  background: i.type==='CRYPTO' ? '#fde4cd' : i.type==='ETF' ? '#d9e4d1' : '#e1dcf0',
                  display:'flex', alignItems:'center', justifyContent:'center',
                  fontFamily:A.serif, fontWeight:500, fontSize:14, color:A.ink,
                }}>{i.tick.slice(0,2)}</div>
                <div style={{ flex:1, minWidth:0 }}>
                  <div style={{ display:'flex', alignItems:'baseline', gap:6 }}>
                    <div style={{ fontFamily:A.sans, fontSize:13, color:A.ink, fontWeight:600 }}>{i.tick}</div>
                    <div style={{ fontFamily:A.sans, fontSize:10, color:A.ink4, letterSpacing:'.12em', textTransform:'uppercase' }}>{i.type}</div>
                  </div>
                  <div style={{ fontFamily:A.sans, fontSize:11.5, color:A.ink3, marginTop:1, whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis' }}>
                    {i.name}
                  </div>
                </div>
                <div style={{ textAlign:'right' }}>
                  <div style={{ fontFamily:A.mono, fontSize:13, color:A.ink }}>{eur(val, {compact:true}).replace('€ ','€')}</div>
                  <div style={{ fontFamily:A.sans, fontSize:11, color: pos?A.green:A.neg, marginTop:1, fontWeight:600 }}>
                    {pos?'+':''}{i.chg.toFixed(2)}%
                  </div>
                </div>
              </div>
            );
          })}
        </ACard>
      </div>
    </div>
  );
}

// ═══ BUDGET ══════════════════════════════════════════════════
function ABudget() {
  return (
    <div style={{ flex:1, overflow:'auto', background:A.bg }}>
      <AHeader
        eyebrow="13 days remaining"
        title="April budget"
        right={<AIconBtn><Ico.cog width="16"/></AIconBtn>}
      />

      {/* Hero ring */}
      <div style={{ padding:'0 16px 16px' }}>
        <ACard>
          <div style={{ padding:'20px 18px', display:'flex', alignItems:'center', gap:18 }}>
            <div style={{ position:'relative', flexShrink:0 }}>
              <ADonut size={120} thick={14} segs={MOCK.categories.filter(c=>c.spent>0).map(c=>({v:c.spent,c:c.color}))}/>
              <div style={{ position:'absolute', inset:0, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center' }}>
                <div style={{ fontFamily:A.serif, fontSize:26, color:A.ink, lineHeight:1, letterSpacing:'-.02em' }}>
                  {((MOCK.budgetSpent/MOCK.budgetTotal)*100).toFixed(0)}%
                </div>
                <div style={{ fontFamily:A.sans, fontSize:9, color:A.ink3, letterSpacing:'.15em', textTransform:'uppercase', marginTop:2 }}>used</div>
              </div>
            </div>
            <div>
              <div style={{ fontFamily:A.sans, fontSize:10, letterSpacing:'.22em', color:A.ink3, textTransform:'uppercase' }}>Spent so far</div>
              <div style={{ fontFamily:A.serif, fontSize:28, color:A.ink, marginTop:3, letterSpacing:'-.02em', lineHeight:1 }}>
                {eur(MOCK.budgetSpent, {compact:true})}
              </div>
              <div style={{ fontFamily:A.sans, fontSize:12, color:A.ink3, marginTop:4 }}>
                of {eur(MOCK.budgetTotal, {compact:true})}
              </div>
              <div style={{
                marginTop:10, display:'inline-flex', alignItems:'center', gap:6,
                fontFamily:A.sans, fontSize:11, color:A.green, fontWeight:600,
                background:A.greenSoft, padding:'4px 10px', borderRadius:100,
              }}>✦ On pace — 17% under</div>
            </div>
          </div>
        </ACard>
      </div>

      {/* Category list */}
      <div style={{ padding:'0 16px 80px' }}>
        <div style={{ fontFamily:A.sans, fontSize:11, letterSpacing:'.14em', color:A.ink3, textTransform:'uppercase', padding:'4px 6px 10px' }}>By category</div>
        <ACard>
          {MOCK.categories.map((c,i)=>{
            const p = c.spent / c.budget;
            const over = p > 1;
            const last = i === MOCK.categories.length - 1;
            return (
              <div key={i} style={{ padding:'14px 16px', borderBottom: last?'none':`1px solid ${A.ruleSoft}` }}>
                <div style={{ display:'flex', alignItems:'center', gap:10 }}>
                  <div style={{ width:28, height:28, borderRadius:100, background: c.color, opacity:.2, display:'flex', alignItems:'center', justifyContent:'center' }}>
                    <span style={{ width:10, height:10, borderRadius:100, background:c.color }}/>
                  </div>
                  <div style={{ flex:1 }}>
                    <div style={{ fontFamily:A.sans, fontSize:14, color:A.ink, fontWeight:500 }}>{c.name}</div>
                    <div style={{ fontFamily:A.sans, fontSize:11, color: over?A.neg:A.ink3, marginTop:1 }}>
                      {over ? `Over by ${eur(c.spent-c.budget).replace('€ ','€')}` : `${eur(c.budget-c.spent).replace('€ ','€')} left`}
                    </div>
                  </div>
                  <div style={{ textAlign:'right', fontFamily:A.mono, fontSize:13 }}>
                    <span style={{ color:A.ink, fontWeight:500 }}>{eur(c.spent,{compact:true}).replace('€ ','€')}</span>
                    <span style={{ color:A.ink4 }}> / {eur(c.budget,{compact:true}).replace('€ ','€')}</span>
                  </div>
                </div>
                <div style={{ marginTop:10, height:5, background:A.ruleSoft, borderRadius:100, overflow:'hidden' }}>
                  <div style={{ width:`${Math.min(p,1)*100}%`, height:'100%', background: over?A.neg:c.color, borderRadius:100 }}/>
                </div>
              </div>
            );
          })}
        </ACard>
      </div>
    </div>
  );
}

Object.assign(window, {
  A, AHome, ATransactions, AAdd, AAccounts, AInvest, ABudget, ATabBar,
});
