import { useMemo, useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import './App.css'

type TicketStatus = 'Open' | 'In progress' | 'Resolved'
type TicketPriority = 'Critical' | 'High' | 'Medium' | 'Low'

type Ticket = {
  id: string
  title: string
  customer: string
  initials: string
  priority: TicketPriority
  status: TicketStatus
  assignee: string
  updated: string
  sla: string
  channel: string
  description: string
}

const seedTickets: Ticket[] = [
  {
    id: 'RH-1048',
    title: 'Payment reconciliation is delayed',
    customer: 'Northstar Labs',
    initials: 'NL',
    priority: 'Critical',
    status: 'In progress',
    assignee: 'Maya Chen',
    updated: '4 min ago',
    sla: '48m left',
    channel: 'API',
    description: 'Reconciliation jobs have remained pending since the latest production deployment.',
  },
  {
    id: 'RH-1047',
    title: 'Invite emails not delivered',
    customer: 'Orbit Finance',
    initials: 'OF',
    priority: 'High',
    status: 'Open',
    assignee: 'Unassigned',
    updated: '19 min ago',
    sla: '2h 12m left',
    channel: 'Email',
    description: 'New workspace members are not receiving their invitation emails.',
  },
  {
    id: 'RH-1046',
    title: 'Audit log export needs filters',
    customer: 'Aster Group',
    initials: 'AG',
    priority: 'Medium',
    status: 'In progress',
    assignee: 'Jon Bell',
    updated: '37 min ago',
    sla: '6h 20m left',
    channel: 'Portal',
    description: 'Compliance needs audit exports filtered by actor, action and date range.',
  },
  {
    id: 'RH-1045',
    title: 'SSO role mapping mismatch',
    customer: 'Helix Cloud',
    initials: 'HC',
    priority: 'High',
    status: 'Open',
    assignee: 'Ava Reed',
    updated: '1h ago',
    sla: '3h 04m left',
    channel: 'Portal',
    description: 'Users assigned the Analyst group are receiving an incorrect workspace role.',
  },
  {
    id: 'RH-1044',
    title: 'Webhook retry policy question',
    customer: 'Kite Commerce',
    initials: 'KC',
    priority: 'Low',
    status: 'Resolved',
    assignee: 'Maya Chen',
    updated: '2h ago',
    sla: 'Met',
    channel: 'Chat',
    description: 'Customer requested clarification about exponential backoff and retry limits.',
  },
]

const iconPaths: Record<string, ReactNode> = {
  grid: <><rect x="3" y="3" width="7" height="7" rx="2"/><rect x="14" y="3" width="7" height="7" rx="2"/><rect x="3" y="14" width="7" height="7" rx="2"/><rect x="14" y="14" width="7" height="7" rx="2"/></>,
  inbox: <><path d="M4 5.5A2.5 2.5 0 0 1 6.5 3h11A2.5 2.5 0 0 1 20 5.5v13A2.5 2.5 0 0 1 17.5 21h-11A2.5 2.5 0 0 1 4 18.5z"/><path d="M4 14h4l2 3h4l2-3h4"/></>,
  ticket: <><path d="M5 4h14a2 2 0 0 1 2 2v3a3 3 0 0 0 0 6v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-3a3 3 0 0 0 0-6V6a2 2 0 0 1 2-2Z"/><path d="M13 8h3M13 12h3M13 16h2M9 8v8"/></>,
  users: <><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></>,
  chart: <><path d="M4 19V9M10 19V5M16 19v-7M22 19V2"/><path d="M2 22h22"/></>,
  settings: <><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-2.83 2.83-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1.03 1.56V21h-4v-.08A1.7 1.7 0 0 0 8.94 19.4a1.7 1.7 0 0 0-1.88.34l-.06.06-2.83-2.83.06-.06A1.7 1.7 0 0 0 4.57 15 1.7 1.7 0 0 0 3 14H3v-4h.08A1.7 1.7 0 0 0 4.6 8.94a1.7 1.7 0 0 0-.34-1.88L4.2 7l2.83-2.83.06.06A1.7 1.7 0 0 0 9 4.57 1.7 1.7 0 0 0 10 3V3h4v.08A1.7 1.7 0 0 0 15.06 4.6a1.7 1.7 0 0 0 1.88-.34L17 4.2 19.83 7l-.06.06A1.7 1.7 0 0 0 19.43 9 1.7 1.7 0 0 0 21 10h.08v4H21a1.7 1.7 0 0 0-1.6 1Z"/></>,
  search: <><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
  plus: <path d="M12 5v14M5 12h14"/>,
  bolt: <path d="m13 2-9 12h7l-1 8 9-12h-7z"/>,
  clock: <><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></>,
  trend: <><path d="m3 17 6-6 4 4 8-8"/><path d="M15 7h6v6"/></>,
  check: <path d="m5 12 4 4L19 6"/>,
  arrow: <path d="m9 18 6-6-6-6"/>,
  bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/></>,
  chevron: <path d="m8 10 4 4 4-4"/>,
  dots: <><circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/></>,
  close: <path d="m6 6 12 12M18 6 6 18"/>,
  command: <><path d="M9 6H7a3 3 0 0 0 0 6h10a3 3 0 0 1 0 6h-2"/><path d="M9 3v18M15 3v18"/></>,
}

function Icon({ name, size = 18 }: { name: string; size?: number }) {
  return <svg aria-hidden="true" className="icon" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">{iconPaths[name]}</svg>
}

function App() {
  const [tickets, setTickets] = useState(seedTickets)
  const [filter, setFilter] = useState<'All' | TicketStatus>('All')
  const [query, setQuery] = useState('')
  const [selectedId, setSelectedId] = useState(seedTickets[0].id)
  const [modalOpen, setModalOpen] = useState(false)
  const [menuOpen, setMenuOpen] = useState(false)

  const visibleTickets = useMemo(() => tickets.filter((ticket) => {
    const matchesFilter = filter === 'All' || ticket.status === filter
    const term = query.toLowerCase().trim()
    const matchesQuery = !term || `${ticket.id} ${ticket.title} ${ticket.customer}`.toLowerCase().includes(term)
    return matchesFilter && matchesQuery
  }), [filter, query, tickets])

  const selected = tickets.find((ticket) => ticket.id === selectedId) ?? tickets[0]

  function createTicket(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const values = new FormData(event.currentTarget)
    const title = String(values.get('title') ?? '').trim()
    const customer = String(values.get('customer') ?? '').trim()
    if (!title || !customer) return

    const nextTicket: Ticket = {
      id: `RH-${1049 + tickets.length - seedTickets.length}`,
      title,
      customer,
      initials: customer.split(/\s+/).map((word) => word[0]).join('').slice(0, 2).toUpperCase(),
      priority: values.get('priority') as TicketPriority,
      status: 'Open',
      assignee: 'Unassigned',
      updated: 'Just now',
      sla: '4h left',
      channel: 'Portal',
      description: String(values.get('description') ?? 'New customer request.'),
    }
    setTickets((current) => [nextTicket, ...current])
    setSelectedId(nextTicket.id)
    setFilter('All')
    setModalOpen(false)
    event.currentTarget.reset()
  }

  return (
    <div className="app-shell">
      <aside className={`sidebar ${menuOpen ? 'is-open' : ''}`}>
        <div className="brand"><span className="brand-mark"><Icon name="command" size={21}/></span><span>ResolveHub</span></div>
        <button className="workspace-switch" type="button"><span className="workspace-avatar">A</span><span><small>Workspace</small>Atlas Support</span><Icon name="chevron" size={15}/></button>

        <nav aria-label="Main navigation">
          <p className="nav-label">Workspace</p>
          <a className="active" href="#overview"><Icon name="grid"/><span>Overview</span></a>
          <a href="#inbox"><Icon name="inbox"/><span>Inbox</span><b>7</b></a>
          <a href="#tickets"><Icon name="ticket"/><span>Tickets</span></a>
          <a href="#customers"><Icon name="users"/><span>Customers</span></a>
          <p className="nav-label nav-secondary">Manage</p>
          <a href="#reports"><Icon name="chart"/><span>Reports</span></a>
          <a href="#settings"><Icon name="settings"/><span>Settings</span></a>
        </nav>

        <div className="sidebar-bottom">
          <div className="system-card"><span className="live-dot"/><div><strong>All systems operational</strong><small>Last checked just now</small></div></div>
          <div className="profile"><span className="profile-avatar">AM</span><div><strong>Alex Morgan</strong><small>Administrator</small></div><Icon name="dots" size={17}/></div>
        </div>
      </aside>

      <main className="main">
        <header className="topbar">
          <button className="menu-button" type="button" aria-label="Toggle navigation" onClick={() => setMenuOpen(!menuOpen)}><span/><span/><span/></button>
          <label className="search"><Icon name="search" size={17}/><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search tickets, customers..."/><kbd>⌘ K</kbd></label>
          <div className="header-actions"><button aria-label="Notifications" className="icon-button"><Icon name="bell"/><span/></button><button className="help-button">Help center</button><button className="new-ticket" onClick={() => setModalOpen(true)}><Icon name="plus" size={17}/>New ticket</button></div>
        </header>

        <div className="content" id="overview">
          <section className="welcome">
            <div><p className="eyebrow">Thursday, 3 September</p><h1>Good morning, Alex.</h1><p>Here is what is happening across your support workspace.</p></div>
            <div className="live-pulse"><span/><strong>Live operations</strong></div>
          </section>

          <section className="metrics" aria-label="Support metrics">
            <article className="metric-card"><div className="metric-icon green"><Icon name="ticket"/></div><div className="metric-top"><span>Open tickets</span><em className="positive"><Icon name="trend" size={13}/>8.2%</em></div><strong>24</strong><small>vs. 22 last week</small></article>
            <article className="metric-card"><div className="metric-icon teal"><Icon name="bolt"/></div><div className="metric-top"><span>SLA health</span><em className="positive"><Icon name="trend" size={13}/>2.1%</em></div><strong>96.4%</strong><small>2 tickets at risk</small></article>
            <article className="metric-card"><div className="metric-icon amber"><Icon name="clock"/></div><div className="metric-top"><span>First response</span><em className="negative">6 min slower</em></div><strong>42m</strong><small>Target under 60 min</small></article>
            <article className="metric-card"><div className="metric-icon blue"><Icon name="check"/></div><div className="metric-top"><span>Resolved</span><em className="positive"><Icon name="trend" size={13}/>14.6%</em></div><strong>118</strong><small>This month</small></article>
          </section>

          <section className="insights-grid">
            <article className="panel workload-panel">
              <div className="panel-heading"><div><h2>Team workload</h2><p>Active ticket ownership today</p></div><button>View team <Icon name="arrow" size={14}/></button></div>
              <div className="workload-list">
                {[
                  ['MC', 'Maya Chen', '12 active', 76, 'mint'],
                  ['JB', 'Jon Bell', '9 active', 58, 'blue'],
                  ['AR', 'Ava Reed', '7 active', 43, 'pink'],
                  ['SL', 'Sam Lee', '5 active', 31, 'amber'],
                ].map(([initials, name, label, width, color]) => <div className="workload-row" key={String(name)}><span className={`member-avatar ${color}`}>{initials}</span><div className="member-info"><div><strong>{name}</strong><small>{label}</small></div><div className="progress"><i style={{width: `${width}%`}}/></div></div></div>)}
              </div>
            </article>

            <article className="panel priority-panel">
              <div className="panel-heading"><div><h2>Tickets by priority</h2><p>Current open workload</p></div><button aria-label="More options"><Icon name="dots"/></button></div>
              <div className="priority-content">
                <div className="donut"><div><strong>24</strong><small>Total</small></div></div>
                <div className="legend">
                  <span><i className="critical"/>Critical <b>3</b></span>
                  <span><i className="high"/>High <b>7</b></span>
                  <span><i className="medium"/>Medium <b>9</b></span>
                  <span><i className="low"/>Low <b>5</b></span>
                </div>
              </div>
              <div className="sla-note"><Icon name="bolt" size={16}/><span><strong>2 tickets are nearing SLA breach</strong><small>Prioritise them within the next hour</small></span><Icon name="arrow" size={15}/></div>
            </article>
          </section>

          <section className="panel ticket-panel" id="tickets">
            <div className="ticket-header"><div><h2>Recent tickets</h2><p>Latest requests across all channels</p></div><div className="filters">{(['All', 'Open', 'In progress', 'Resolved'] as const).map((option) => <button key={option} className={filter === option ? 'active' : ''} onClick={() => setFilter(option)}>{option}</button>)}</div></div>
            <div className="ticket-layout">
              <div className="ticket-table-wrap">
                <table>
                  <thead><tr><th>Ticket</th><th>Priority</th><th>Status</th><th>Assignee</th><th>Updated</th><th>SLA</th></tr></thead>
                  <tbody>
                    {visibleTickets.map((ticket) => <tr key={ticket.id} className={selected.id === ticket.id ? 'selected' : ''} onClick={() => setSelectedId(ticket.id)}>
                      <td><div className="ticket-title"><span className="customer-avatar">{ticket.initials}</span><div><strong>{ticket.title}</strong><small>{ticket.id} · {ticket.customer}</small></div></div></td>
                      <td><span className={`priority ${ticket.priority.toLowerCase()}`}><i/>{ticket.priority}</span></td>
                      <td><span className={`status ${ticket.status.toLowerCase().replace(' ', '-')}`}>{ticket.status}</span></td>
                      <td>{ticket.assignee}</td><td>{ticket.updated}</td><td><span className={ticket.sla.includes('48') ? 'sla-risk' : ''}>{ticket.sla}</span></td>
                    </tr>)}
                  </tbody>
                </table>
                {visibleTickets.length === 0 && <div className="empty-state"><Icon name="search" size={24}/><strong>No matching tickets</strong><span>Try a different search or status filter.</span></div>}
              </div>

              <aside className="ticket-detail">
                <div className="detail-top"><span className={`priority ${selected.priority.toLowerCase()}`}><i/>{selected.priority}</span><button aria-label="Close details"><Icon name="close" size={17}/></button></div>
                <small>{selected.id} · {selected.channel}</small><h3>{selected.title}</h3><p>{selected.description}</p>
                <dl><div><dt>Customer</dt><dd>{selected.customer}</dd></div><div><dt>Owner</dt><dd>{selected.assignee}</dd></div><div><dt>SLA target</dt><dd>{selected.sla}</dd></div><div><dt>Status</dt><dd><span className={`status ${selected.status.toLowerCase().replace(' ', '-')}`}>{selected.status}</span></dd></div></dl>
                <div className="activity"><span/><div><strong>Ticket updated</strong><small>{selected.updated}</small></div></div>
                <button className="open-ticket">Open full ticket <Icon name="arrow" size={15}/></button>
              </aside>
            </div>
          </section>
        </div>
      </main>

      {modalOpen && <div className="modal-backdrop" onMouseDown={() => setModalOpen(false)}>
        <div className="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title" onMouseDown={(event) => event.stopPropagation()}>
          <div className="modal-heading"><div><span className="modal-icon"><Icon name="ticket"/></span><div><h2 id="modal-title">Create a new ticket</h2><p>Add a customer request to the support queue.</p></div></div><button aria-label="Close" onClick={() => setModalOpen(false)}><Icon name="close"/></button></div>
          <form onSubmit={createTicket}>
            <label>Ticket title<input name="title" required placeholder="e.g. Production report unavailable"/></label>
            <div className="form-row"><label>Customer<input name="customer" required placeholder="Company name"/></label><label>Priority<select name="priority" defaultValue="High"><option>Critical</option><option>High</option><option>Medium</option><option>Low</option></select></label></div>
            <label>Description<textarea name="description" rows={4} placeholder="Describe the issue and its business impact..."/></label>
            <div className="modal-actions"><button type="button" onClick={() => setModalOpen(false)}>Cancel</button><button type="submit">Create ticket</button></div>
          </form>
        </div>
      </div>}
    </div>
  )
}

export default App
