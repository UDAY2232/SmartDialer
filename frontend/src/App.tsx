import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import './index.css';

// --- Icons from lucide-react ---
import {
  LayoutDashboard, Users, Phone, Zap, ShieldCheck, Radio,
  FlaskConical, BarChart3, ChevronDown, Activity,
  AlertTriangle, CheckCircle2, XCircle, ArrowRight, Play, ServerCrash,
  Database, Clock, ArrowDownRight, Loader2, WifiOff, UserMinus, UserPlus,
  PhoneIncoming, PhoneOff
} from 'lucide-react';

// === API BASE ===
const API = import.meta.env.VITE_API_URL || 'http://localhost:8082';

// === TYPES ===
interface DashboardStats {
  availableAgents: number;
  totalAgents: number;
  activeCalls: number;
  connectedCalls: number;
  completedCalls: number;
  failedCalls: number;
  totalCalls: number;
  answerRate: number;
  agentUtilization: number;
  reservedAgents: number;
  dialingAgents: number;
  connectedAgents: number;
  offlineAgents: number;
  providerAHealth: string;
  providerBHealth: string;
}

interface PacingDecision {
  recommendedCalls: number;
  reasoning: string;
  campaignId?: number;
}

interface SafetyDecision {
  decision: string;
  approvedCalls: number;
  requestedCalls: number;
  reasoning: string;
}

interface SimulationResult {
  pacingDecision: PacingDecision;
  safetyDecision: SafetyDecision;
}

interface AnalyticsStats {
  estimatedAnswerRate: string;
  averageTalkTime: string;
  dataSource: string;
  totalHistoricalCalls: string;
  totalAnsweredCalls: string;
  recentAnswerRate: string;
  providerPerformance: string;
  campaignStatistics: string;
}

interface AgentData {
  id: number;
  name: string;
  status: string;
  currentCallId: number | null;
  lastStateChange: string | null;
}

interface CallData {
  id: number;
  campaignId: number;
  borrowerId: number;
  agentId: number;
  provider: string | null;
  providerCallId: string | null;
  state: string;
  failureReason: string | null;
  createdAt: string;
  updatedAt: string;
}

// === NAV ITEMS ===
const NAV_ITEMS = [
  { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { id: 'agents', label: 'Agents', icon: Users },
  { id: 'calls', label: 'Calls', icon: Phone },
  { id: 'pacing', label: 'Predictive Pacing', icon: Zap },
  { id: 'safety', label: 'Safety Controller', icon: ShieldCheck },
  { id: 'providers', label: 'Providers', icon: Radio },
  { id: 'simulation', label: 'Simulation Lab', icon: FlaskConical },
  { id: 'analytics', label: 'Historical Analytics', icon: BarChart3 },
];

// ============================================================
// APP SHELL
// ============================================================
function App() {
  const [activePage, setActivePage] = useState('dashboard');
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [stats, setStats] = useState<DashboardStats>({
    availableAgents: 0, totalAgents: 0, activeCalls: 0, connectedCalls: 0,
    completedCalls: 0, failedCalls: 0, totalCalls: 0, answerRate: 0,
    agentUtilization: 0, reservedAgents: 0, dialingAgents: 0,
    connectedAgents: 0, offlineAgents: 0, providerAHealth: 'HEALTHY', providerBHealth: 'HEALTHY'
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [simResult, setSimResult] = useState<SimulationResult | null>(null);
  const [runningPacing, setRunningPacing] = useState(false);

  const fetchStats = useCallback(async () => {
    try {
      const res = await axios.get(`${API}/api/dashboard`);
      setStats(res.data);
      setError(null);
      setLoading(false);
    } catch {
      setError('Backend unreachable');
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 5000);
    return () => clearInterval(interval);
  }, [fetchStats]);

  const runPacingCycle = async () => {
    setRunningPacing(true);
    try {
      const res = await axios.post(`${API}/api/simulation/run`);
      setSimResult(res.data);
      await fetchStats();
    } catch {
      setError('Failed to run pacing cycle');
    } finally {
      setRunningPacing(false);
    }
  };

  const systemHealthy = !error;

  return (
    <div className="flex h-screen bg-navy-50 font-sans overflow-hidden">
      {/* ---- SIDEBAR ---- */}
      <aside className={`${sidebarCollapsed ? 'w-16' : 'w-60'} bg-navy-900 text-white flex flex-col transition-all duration-200 shrink-0`}>
        {/* Logo */}
        <div className="h-16 flex items-center px-4 border-b border-white/10">
          <div className="flex items-center gap-2.5 overflow-hidden">
            <div className="w-8 h-8 rounded-lg bg-brand-500 flex items-center justify-center shrink-0">
              <Phone className="w-4 h-4 text-white" />
            </div>
            {!sidebarCollapsed && <span className="text-base font-bold tracking-tight whitespace-nowrap">SmartDialer</span>}
          </div>
        </div>

        {/* Nav links */}
        <nav className="flex-1 py-3 px-2 space-y-0.5 overflow-y-auto">
          {NAV_ITEMS.map(item => {
            const Icon = item.icon;
            const active = activePage === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActivePage(item.id)}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors cursor-pointer
                  ${active
                    ? 'bg-brand-600 text-white shadow-lg shadow-brand-600/25'
                    : 'text-navy-400 hover:bg-white/5 hover:text-white'
                  }`}
                title={sidebarCollapsed ? item.label : undefined}
              >
                <Icon className="w-[18px] h-[18px] shrink-0" />
                {!sidebarCollapsed && <span className="whitespace-nowrap">{item.label}</span>}
              </button>
            );
          })}
        </nav>

        {/* Collapse toggle */}
        <button
          onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
          className="h-12 flex items-center justify-center border-t border-white/10 text-navy-400 hover:text-white transition-colors cursor-pointer"
        >
          <ChevronDown className={`w-4 h-4 transition-transform ${sidebarCollapsed ? 'rotate-[-90deg]' : 'rotate-90'}`} />
        </button>
      </aside>

      {/* ---- MAIN CONTENT ---- */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top bar */}
        <header className="h-16 bg-white border-b border-navy-200 flex items-center justify-between px-6 shrink-0">
          <h1 className="text-lg font-semibold text-navy-900">
            {NAV_ITEMS.find(n => n.id === activePage)?.label || 'Dashboard'}
          </h1>
          <div className="flex items-center gap-4">
            <div className={`flex items-center gap-2 text-xs font-medium px-3 py-1.5 rounded-full ${systemHealthy ? 'bg-success-50 text-success-600' : 'bg-danger-50 text-danger-600'}`}>
              <span className={`w-2 h-2 rounded-full ${systemHealthy ? 'bg-success-500 animate-pulse' : 'bg-danger-500'}`} />
              {systemHealthy ? 'System Healthy' : 'Connection Issue'}
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-6">
          {activePage === 'dashboard' && (
            <DashboardPage stats={stats} loading={loading} error={error} simResult={simResult} onRunPacing={runPacingCycle} runningPacing={runningPacing} onRefresh={fetchStats} />
          )}
          {activePage === 'agents' && <AgentsPage />}
          {activePage === 'calls' && <CallsPage />}
          {activePage === 'pacing' && (
            <PacingPage stats={stats} simResult={simResult} onRunPacing={runPacingCycle} runningPacing={runningPacing} />
          )}
          {activePage === 'safety' && (
            <SafetyPage simResult={simResult} onRunPacing={runPacingCycle} runningPacing={runningPacing} />
          )}
          {activePage === 'providers' && <ProvidersPage />}
          {activePage === 'simulation' && <SimulationPage />}
          {activePage === 'analytics' && <AnalyticsPage />}
        </main>
      </div>
    </div>
  );
}

// ============================================================
// REUSABLE COMPONENTS
// ============================================================

function Card({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return <div className={`bg-white rounded-xl border border-navy-200 shadow-sm ${className}`}>{children}</div>;
}

function KPICard({ icon, title, value, subtitle, accent = 'brand' }: {
  icon: React.ReactNode; title: string; value: string | number; subtitle?: string;
  accent?: 'brand' | 'success' | 'warning' | 'danger';
}) {
  const colors = {
    brand: 'bg-brand-50 text-brand-600',
    success: 'bg-success-50 text-success-600',
    warning: 'bg-warning-50 text-warning-600',
    danger: 'bg-danger-50 text-danger-600',
  };
  return (
    <Card className="p-5">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-medium text-navy-500 uppercase tracking-wide">{title}</p>
          <p className="text-2xl font-bold text-navy-900 mt-1">{value}</p>
          {subtitle && <p className="text-xs text-navy-400 mt-1">{subtitle}</p>}
        </div>
        <div className={`p-2.5 rounded-lg ${colors[accent]}`}>
          {icon}
        </div>
      </div>
    </Card>
  );
}

function StatusBadge({ status }: { status: string }) {
  const s = status.toUpperCase();
  const styles: Record<string, string> = {
    APPROVE: 'bg-success-50 text-success-600 border-success-500/20',
    APPROVED: 'bg-success-50 text-success-600 border-success-500/20',
    REDUCE: 'bg-warning-50 text-warning-600 border-warning-500/20',
    REDUCED: 'bg-warning-50 text-warning-600 border-warning-500/20',
    REJECT: 'bg-danger-50 text-danger-600 border-danger-500/20',
    REJECTED: 'bg-danger-50 text-danger-600 border-danger-500/20',
    HEALTHY: 'bg-success-50 text-success-600 border-success-500/20',
    UP: 'bg-success-50 text-success-600 border-success-500/20',
    DEGRADED: 'bg-warning-50 text-warning-600 border-warning-500/20',
    DOWN: 'bg-danger-50 text-danger-600 border-danger-500/20',
    FALLBACK: 'bg-warning-50 text-warning-600 border-warning-500/20',
    AVAILABLE: 'bg-success-50 text-success-600 border-success-500/20',
    RESERVED: 'bg-brand-50 text-brand-600 border-brand-500/20',
    DIALING: 'bg-warning-50 text-warning-600 border-warning-500/20',
    CONNECTED: 'bg-success-50 text-success-600 border-success-500/20',
    WRAP_UP: 'bg-warning-50 text-warning-600 border-warning-500/20',
    OFFLINE: 'bg-danger-50 text-danger-600 border-danger-500/20',
    PAUSED: 'bg-navy-100 text-navy-600 border-navy-200',
    QUEUED: 'bg-navy-100 text-navy-600 border-navy-200',
    INITIATED: 'bg-brand-50 text-brand-600 border-brand-500/20',
    RINGING: 'bg-warning-50 text-warning-600 border-warning-500/20',
    ANSWERED: 'bg-success-50 text-success-600 border-success-500/20',
    COMPLETED: 'bg-navy-100 text-navy-600 border-navy-200',
    CANCELLED: 'bg-danger-50 text-danger-600 border-danger-500/20',
    FAILED: 'bg-danger-50 text-danger-600 border-danger-500/20',
  };
  return (
    <span className={`inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold border ${styles[s] || 'bg-navy-100 text-navy-600 border-navy-200'}`}>
      {s}
    </span>
  );
}

function EmptyState({ message }: { message: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-navy-400">
      <Activity className="w-10 h-10 mb-3 opacity-40" />
      <p className="text-sm">{message}</p>
    </div>
  );
}

function ErrorBanner({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="flex items-center justify-between bg-danger-50 border border-danger-500/20 rounded-lg px-4 py-3">
      <div className="flex items-center gap-2">
        <WifiOff className="w-4 h-4 text-danger-600" />
        <span className="text-sm font-medium text-danger-600">{message}</span>
      </div>
      {onRetry && (
        <button onClick={onRetry} className="text-xs font-medium text-danger-600 hover:text-danger-500 underline cursor-pointer">
          Retry
        </button>
      )}
    </div>
  );
}

function ProgressBar({ value, max = 100, color = 'brand' }: { value: number; max?: number; color?: string }) {
  const pct = Math.min(100, Math.max(0, (value / max) * 100));
  const barColor = color === 'success' ? 'bg-success-500' : color === 'warning' ? 'bg-warning-500' : 'bg-brand-500';
  return (
    <div className="w-full h-2 bg-navy-100 rounded-full overflow-hidden">
      <div className={`h-full rounded-full transition-all duration-500 ${barColor}`} style={{ width: `${pct}%` }} />
    </div>
  );
}


// ============================================================
// DASHBOARD PAGE
// ============================================================

function DashboardPage({ stats, loading, error, simResult, onRunPacing, runningPacing, onRefresh }: {
  stats: DashboardStats; loading: boolean; error: string | null;
  simResult: SimulationResult | null; onRunPacing: () => void; runningPacing: boolean;
  onRefresh: () => void;
}) {
  if (loading) {
    return <div className="flex items-center justify-center h-64"><Loader2 className="w-8 h-8 text-brand-500 animate-spin" /></div>;
  }

  return (
    <div className="space-y-6 max-w-7xl">
      {error && <ErrorBanner message={error} onRetry={onRefresh} />}

      {/* Hero statement */}
      <div className="bg-navy-900 text-white rounded-xl p-6">
        <p className="text-sm font-medium text-navy-300 mb-1">CORE PRINCIPLE</p>
        <p className="text-base font-medium leading-relaxed">
          SmartDialer <span className="text-brand-400 font-bold">predicts</span> how aggressively to dial, but the{' '}
          <span className="text-success-500 font-bold">Safety Controller</span> decides what is actually safe.
        </p>
      </div>

      {/* KPI Grid — Row 1: Core Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <KPICard
          icon={<Users className="w-5 h-5" />}
          title="Available Agents"
          value={`${stats.availableAgents} / ${stats.totalAgents}`}
          subtitle={`${stats.offlineAgents} offline · ${stats.reservedAgents} reserved · ${stats.dialingAgents} dialing`}
          accent="brand"
        />
        <KPICard
          icon={<Phone className="w-5 h-5" />}
          title="Active Calls"
          value={stats.activeCalls}
          subtitle={`${stats.connectedCalls} connected · ${stats.completedCalls} completed · ${stats.failedCalls} failed`}
          accent="success"
        />
        <KPICard
          icon={<Activity className="w-5 h-5" />}
          title="Historical Answer Rate"
          value={`${stats.answerRate}%`}
          subtitle="From provided 30k dataset"
          accent="warning"
        />
        <Card className="p-5">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <p className="text-xs font-medium text-navy-500 uppercase tracking-wide">Agent Utilization</p>
              <p className="text-2xl font-bold text-navy-900 mt-1">{stats.agentUtilization}%</p>
              <div className="mt-2">
                <ProgressBar value={stats.agentUtilization} color={stats.agentUtilization > 80 ? 'success' : 'brand'} />
              </div>
            </div>
            <div className="p-2.5 rounded-lg bg-brand-50 text-brand-600 ml-3">
              <BarChart3 className="w-5 h-5" />
            </div>
          </div>
        </Card>
      </div>

      {/* Provider Health Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <Card className="p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Radio className="w-4 h-4 text-navy-500" />
              <span className="text-sm font-semibold text-navy-900">Provider A</span>
            </div>
            <StatusBadge status={stats.providerAHealth} />
          </div>
        </Card>
        <Card className="p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Radio className="w-4 h-4 text-navy-500" />
              <span className="text-sm font-semibold text-navy-900">Provider B</span>
            </div>
            <StatusBadge status={stats.providerBHealth} />
          </div>
        </Card>
      </div>

      {/* Dialer Engine Panel */}
      <Card className="p-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-base font-bold text-navy-900">Dialer Engine</h2>
            <p className="text-sm text-navy-500 mt-0.5">Run a predictive pacing cycle to calculate safe dial capacity</p>
          </div>
          <button
            onClick={onRunPacing}
            disabled={runningPacing}
            className="flex items-center gap-2 bg-brand-600 hover:bg-brand-500 disabled:opacity-60 text-white px-5 py-2.5 rounded-lg text-sm font-semibold transition-colors cursor-pointer disabled:cursor-not-allowed"
          >
            {runningPacing ? <Loader2 className="w-4 h-4 animate-spin" /> : <Zap className="w-4 h-4" />}
            Run Pacing Cycle
          </button>
        </div>
      </Card>

      {/* Pacing + Safety result */}
      {simResult && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Pacing result */}
          <Card className="p-6 border-l-4 border-l-brand-500">
            <div className="flex items-center gap-2 mb-4">
              <Zap className="w-5 h-5 text-brand-600" />
              <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide">Pacing Recommendation</h3>
            </div>
            <p className="text-4xl font-black text-brand-600">
              {simResult.pacingDecision.recommendedCalls}
              <span className="text-sm font-medium text-navy-500 ml-2">calls recommended</span>
            </p>
            <div className="mt-4 bg-brand-50 rounded-lg p-3">
              <p className="text-xs text-brand-600 font-medium">{simResult.pacingDecision.reasoning}</p>
            </div>
          </Card>

          {/* Safety result */}
          <Card className="p-6 border-l-4 border-l-success-500">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-success-600" />
                <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide">Safety Controller</h3>
              </div>
              <StatusBadge status={simResult.safetyDecision.decision} />
            </div>
            <div className="flex items-baseline gap-3">
              <p className="text-4xl font-black text-success-600">{simResult.safetyDecision.approvedCalls}</p>
              <span className="text-sm font-medium text-navy-500">safe calls approved</span>
            </div>
            <div className="mt-4 bg-success-50 rounded-lg p-3">
              <p className="text-xs text-success-600 font-medium">{simResult.safetyDecision.reasoning}</p>
            </div>
          </Card>
        </div>
      )}

      {!simResult && (
        <EmptyState message="Run a pacing cycle to see the Predictive Pacing → Safety Controller pipeline" />
      )}
    </div>
  );
}

// ============================================================
// AGENTS PAGE — User 2's primary view
// ============================================================

function AgentsPage() {
  const [agents, setAgents] = useState<AgentData[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchAgents = useCallback(async () => {
    try {
      const res = await axios.get(`${API}/api/agents`);
      setAgents(res.data);
      setLoading(false);
    } catch {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAgents();
    const interval = setInterval(fetchAgents, 3000);
    return () => clearInterval(interval);
  }, [fetchAgents]);

  if (loading) {
    return <div className="flex items-center justify-center h-64"><Loader2 className="w-8 h-8 text-brand-500 animate-spin" /></div>;
  }

  const statusCounts: Record<string, number> = {};
  agents.forEach(a => {
    statusCounts[a.status] = (statusCounts[a.status] || 0) + 1;
  });

  return (
    <div className="space-y-6 max-w-6xl">
      <div>
        <h2 className="text-2xl font-bold text-navy-900">Agent Management</h2>
        <p className="text-sm text-navy-500 mt-1">Real-time agent states from the database — polls every 3 seconds</p>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
        {['AVAILABLE', 'RESERVED', 'DIALING', 'CONNECTED', 'WRAP_UP', 'OFFLINE'].map(status => (
          <Card key={status} className="p-3 text-center">
            <p className="text-xs text-navy-500 font-medium">{status}</p>
            <p className="text-2xl font-bold text-navy-900 mt-1">{statusCounts[status] || 0}</p>
          </Card>
        ))}
      </div>

      {/* Agent table */}
      <Card className="overflow-hidden">
        <div className="px-5 py-3 border-b border-navy-200 bg-navy-50">
          <span className="text-xs font-bold text-navy-500 uppercase tracking-wide">
            All Agents ({agents.length})
          </span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-navy-100">
                <th className="text-left px-5 py-3 text-xs font-bold text-navy-500 uppercase">ID</th>
                <th className="text-left px-5 py-3 text-xs font-bold text-navy-500 uppercase">Name</th>
                <th className="text-left px-5 py-3 text-xs font-bold text-navy-500 uppercase">Status</th>
                <th className="text-left px-5 py-3 text-xs font-bold text-navy-500 uppercase">Current Call</th>
                <th className="text-left px-5 py-3 text-xs font-bold text-navy-500 uppercase">Last State Change</th>
              </tr>
            </thead>
            <tbody>
              {agents.map(agent => (
                <tr key={agent.id} className="border-b border-navy-100 hover:bg-navy-50 transition-colors">
                  <td className="px-5 py-3 font-mono text-navy-600">#{agent.id}</td>
                  <td className="px-5 py-3 font-medium text-navy-900">{agent.name}</td>
                  <td className="px-5 py-3"><StatusBadge status={agent.status} /></td>
                  <td className="px-5 py-3 text-navy-600">{agent.currentCallId ? `Call #${agent.currentCallId}` : '—'}</td>
                  <td className="px-5 py-3 text-navy-400 text-xs">
                    {agent.lastStateChange ? new Date(agent.lastStateChange).toLocaleTimeString() : '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}

// ============================================================
// CALLS PAGE — User 2's call lifecycle view
// ============================================================

function CallsPage() {
  const [calls, setCalls] = useState<CallData[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchCalls = useCallback(async () => {
    try {
      const res = await axios.get(`${API}/api/calls`);
      setCalls(res.data);
      setLoading(false);
    } catch {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCalls();
    const interval = setInterval(fetchCalls, 3000);
    return () => clearInterval(interval);
  }, [fetchCalls]);

  if (loading) {
    return <div className="flex items-center justify-center h-64"><Loader2 className="w-8 h-8 text-brand-500 animate-spin" /></div>;
  }

  const stateCounts: Record<string, number> = {};
  calls.forEach(c => {
    stateCounts[c.state] = (stateCounts[c.state] || 0) + 1;
  });

  // Show most recent calls first
  const sortedCalls = [...calls].sort((a, b) => b.id - a.id);

  return (
    <div className="space-y-6 max-w-7xl">
      <div>
        <h2 className="text-2xl font-bold text-navy-900">Call Management</h2>
        <p className="text-sm text-navy-500 mt-1">Real-time call states from the database — polls every 3 seconds</p>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-3 sm:grid-cols-5 lg:grid-cols-9 gap-3">
        {['QUEUED', 'RESERVED', 'INITIATED', 'RINGING', 'ANSWERED', 'CONNECTED', 'COMPLETED', 'CANCELLED', 'FAILED'].map(state => (
          <Card key={state} className="p-3 text-center">
            <p className="text-[10px] text-navy-500 font-medium">{state}</p>
            <p className="text-xl font-bold text-navy-900 mt-1">{stateCounts[state] || 0}</p>
          </Card>
        ))}
      </div>

      {/* Calls table */}
      <Card className="overflow-hidden">
        <div className="px-5 py-3 border-b border-navy-200 bg-navy-50">
          <span className="text-xs font-bold text-navy-500 uppercase tracking-wide">
            All Calls ({calls.length})
          </span>
        </div>
        <div className="overflow-x-auto max-h-[500px] overflow-y-auto">
          <table className="w-full text-sm">
            <thead className="sticky top-0 bg-white">
              <tr className="border-b border-navy-100">
                <th className="text-left px-4 py-3 text-xs font-bold text-navy-500 uppercase">ID</th>
                <th className="text-left px-4 py-3 text-xs font-bold text-navy-500 uppercase">State</th>
                <th className="text-left px-4 py-3 text-xs font-bold text-navy-500 uppercase">Agent</th>
                <th className="text-left px-4 py-3 text-xs font-bold text-navy-500 uppercase">Borrower</th>
                <th className="text-left px-4 py-3 text-xs font-bold text-navy-500 uppercase">Provider</th>
                <th className="text-left px-4 py-3 text-xs font-bold text-navy-500 uppercase">Failure</th>
                <th className="text-left px-4 py-3 text-xs font-bold text-navy-500 uppercase">Created</th>
                <th className="text-left px-4 py-3 text-xs font-bold text-navy-500 uppercase">Updated</th>
              </tr>
            </thead>
            <tbody>
              {sortedCalls.map(call => (
                <tr key={call.id} className="border-b border-navy-100 hover:bg-navy-50 transition-colors">
                  <td className="px-4 py-3 font-mono text-navy-600">#{call.id}</td>
                  <td className="px-4 py-3"><StatusBadge status={call.state} /></td>
                  <td className="px-4 py-3 text-navy-600">#{call.agentId}</td>
                  <td className="px-4 py-3 text-navy-600">#{call.borrowerId}</td>
                  <td className="px-4 py-3 text-navy-600">{call.provider || '—'}</td>
                  <td className="px-4 py-3 text-danger-600 text-xs">{call.failureReason || '—'}</td>
                  <td className="px-4 py-3 text-navy-400 text-xs">{call.createdAt ? new Date(call.createdAt).toLocaleTimeString() : '—'}</td>
                  <td className="px-4 py-3 text-navy-400 text-xs">{call.updatedAt ? new Date(call.updatedAt).toLocaleTimeString() : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {calls.length === 0 && <EmptyState message="No calls yet. Run a pacing cycle to generate calls." />}
      </Card>
    </div>
  );
}

// ============================================================
// PREDICTIVE PACING PAGE
// ============================================================

function PacingPage({ stats, simResult, onRunPacing, runningPacing }: {
  stats: DashboardStats; simResult: SimulationResult | null; onRunPacing: () => void; runningPacing: boolean;
}) {
  const [analyticsStats, setAnalyticsStats] = useState<AnalyticsStats | null>(null);

  useEffect(() => {
    axios.get(`${API}/api/analytics/historical`).then(r => setAnalyticsStats(r.data)).catch(() => {});
  }, []);

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h2 className="text-2xl font-bold text-navy-900">Predictive Pacing Engine</h2>
        <p className="text-sm text-navy-500 mt-1">Uses historical dataset metrics to predict optimal dial rate</p>
      </div>

      {/* Inputs */}
      <Card className="p-6">
        <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide mb-4">Current Inputs</h3>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
          {[
            { label: 'Historical Answer Rate', value: analyticsStats?.estimatedAnswerRate || `${stats.answerRate}%` },
            { label: 'Avg Talk Time', value: analyticsStats?.averageTalkTime || '-' },
            { label: 'Available Agents', value: stats.availableAgents },
            { label: 'Active Calls', value: stats.activeCalls },
            { label: 'Provider A', value: stats.providerAHealth },
          ].map((item, i) => (
            <div key={i} className="text-center p-3 bg-navy-50 rounded-lg">
              <p className="text-xs text-navy-500 font-medium">{item.label}</p>
              <p className="text-lg font-bold text-navy-900 mt-1">{item.value}</p>
            </div>
          ))}
        </div>
      </Card>

      {/* Action */}
      <div className="flex justify-center">
        <button
          onClick={onRunPacing}
          disabled={runningPacing}
          className="flex items-center gap-2 bg-brand-600 hover:bg-brand-500 disabled:opacity-60 text-white px-8 py-3 rounded-lg text-sm font-bold transition-colors cursor-pointer disabled:cursor-not-allowed shadow-lg shadow-brand-600/25"
        >
          {runningPacing ? <Loader2 className="w-4 h-4 animate-spin" /> : <Zap className="w-4 h-4" />}
          Run Pacing Cycle
        </button>
      </div>

      {/* Flow visualization */}
      {simResult && (
        <div className="space-y-4">
          <div className="flex flex-col items-center gap-2">
            <Card className="px-5 py-3 text-center">
              <p className="text-xs text-navy-500 font-medium">Recommended Calls</p>
              <p className="text-3xl font-black text-brand-600">{simResult.pacingDecision.recommendedCalls}</p>
            </Card>
            <ArrowDownRight className="w-5 h-5 text-navy-300 rotate-[45deg]" />
            <Card className="px-5 py-3 text-center border-success-500/30">
              <p className="text-xs text-navy-500 font-medium">Safety Approved</p>
              <p className="text-3xl font-black text-success-600">{simResult.safetyDecision.approvedCalls}</p>
            </Card>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card className="p-5">
              <h4 className="text-xs font-bold text-navy-500 uppercase mb-2">Pacing Reasoning</h4>
              <p className="text-sm text-navy-700">{simResult.pacingDecision.reasoning}</p>
            </Card>
            <Card className="p-5">
              <h4 className="text-xs font-bold text-navy-500 uppercase mb-2">Safety Reasoning</h4>
              <p className="text-sm text-navy-700">{simResult.safetyDecision.reasoning}</p>
            </Card>
          </div>
        </div>
      )}

      {!simResult && <EmptyState message="Run a pacing cycle to see the prediction pipeline" />}
    </div>
  );
}

// ============================================================
// SAFETY CONTROLLER PAGE
// ============================================================

function SafetyPage({ simResult, onRunPacing, runningPacing }: {
  simResult: SimulationResult | null; onRunPacing: () => void; runningPacing: boolean;
}) {
  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h2 className="text-2xl font-bold text-navy-900">Safety Controller</h2>
        <p className="text-sm text-navy-500 mt-1">The independent safety gate that cannot be bypassed by the pacing engine</p>
      </div>

      {/* Safety rules */}
      <Card className="p-6">
        <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide mb-4">Enforcement Rules</h3>
        <div className="space-y-3">
          {[
            { rule: 'Max Over-dial Ratio', desc: 'Never dial more than 2× available agents', icon: <ShieldCheck className="w-4 h-4" /> },
            { rule: 'Zero Agent Check', desc: 'Reject all calls if no agents are available', icon: <XCircle className="w-4 h-4" /> },
            { rule: 'Provider Health Gate', desc: 'Reject if provider is DOWN; fallback to 1:1 if DEGRADED', icon: <Radio className="w-4 h-4" /> },
          ].map((item, i) => (
            <div key={i} className="flex items-start gap-3 p-3 bg-navy-50 rounded-lg">
              <div className="mt-0.5 text-success-600">{item.icon}</div>
              <div>
                <p className="text-sm font-semibold text-navy-900">{item.rule}</p>
                <p className="text-xs text-navy-500">{item.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </Card>

      {!simResult && (
        <div className="text-center py-8">
          <p className="text-sm text-navy-500 mb-4">Run a pacing cycle to see the Safety Controller decision</p>
          <button
            onClick={onRunPacing}
            disabled={runningPacing}
            className="flex items-center gap-2 mx-auto bg-brand-600 hover:bg-brand-500 disabled:opacity-60 text-white px-6 py-2.5 rounded-lg text-sm font-semibold transition-colors cursor-pointer disabled:cursor-not-allowed"
          >
            {runningPacing ? <Loader2 className="w-4 h-4 animate-spin" /> : <Zap className="w-4 h-4" />}
            Run Pacing Cycle
          </button>
        </div>
      )}

      {simResult && (
        <Card className="p-6">
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide">Last Decision</h3>
            <StatusBadge status={simResult.safetyDecision.decision} />
          </div>

          <div className="grid grid-cols-3 gap-6 text-center mb-6">
            <div>
              <p className="text-xs text-navy-500 font-medium">Requested</p>
              <p className="text-3xl font-black text-navy-900">{simResult.safetyDecision.requestedCalls}</p>
            </div>
            <div>
              <ArrowRight className="w-6 h-6 text-navy-300 mx-auto mt-4" />
            </div>
            <div>
              <p className="text-xs text-navy-500 font-medium">Approved</p>
              <p className="text-3xl font-black text-success-600">{simResult.safetyDecision.approvedCalls}</p>
            </div>
          </div>

          <div className="bg-navy-50 rounded-lg p-4">
            <p className="text-xs font-bold text-navy-500 uppercase mb-1">Reason</p>
            <p className="text-sm text-navy-700">{simResult.safetyDecision.reasoning}</p>
          </div>
        </Card>
      )}
    </div>
  );
}

// ============================================================
// PROVIDERS PAGE — fetches health from backend, not local state
// ============================================================

function ProvidersPage() {
  const [providerA, setProviderA] = useState<string>('HEALTHY');
  const [providerB, setProviderB] = useState<string>('HEALTHY');

  const fetchHealth = useCallback(async () => {
    try {
      const res = await axios.get(`${API}/api/failure/provider-health`);
      setProviderA(res.data.providerA);
      setProviderB(res.data.providerB);
    } catch { /* ignore */ }
  }, []);

  useEffect(() => {
    fetchHealth();
    const interval = setInterval(fetchHealth, 3000);
    return () => clearInterval(interval);
  }, [fetchHealth]);

  const triggerAction = async (action: string, provider: string) => {
    try {
      const endpoint = action === 'outage' ? 'provider-outage' : action === 'degrade' ? 'provider-degrade' : 'provider-recover';
      await axios.post(`${API}/api/failure/${endpoint}/${provider}`);
      // Immediately re-fetch to confirm
      await fetchHealth();
    } catch { /* ignore */ }
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h2 className="text-2xl font-bold text-navy-900">Provider Health</h2>
        <p className="text-sm text-navy-500 mt-1">Monitor telecom provider status — fetched from backend every 3 seconds</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {[
          { name: 'Provider A', status: providerA, uptime: '99.2%', id: 'A' },
          { name: 'Provider B', status: providerB, uptime: '91.4%', id: 'B' },
        ].map(p => (
          <Card key={p.id} className="p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className={`w-3 h-3 rounded-full ${p.status === 'HEALTHY' ? 'bg-success-500 animate-pulse' : p.status === 'DEGRADED' ? 'bg-warning-500' : 'bg-danger-500'}`} />
                <h3 className="text-base font-bold text-navy-900">{p.name}</h3>
              </div>
              <StatusBadge status={p.status} />
            </div>
            <p className="text-sm text-navy-500 mb-4">Historical uptime: <span className="font-semibold text-navy-700">{p.uptime}</span></p>
            <div className="flex gap-2">
              <button
                onClick={() => triggerAction('outage', p.id)}
                className="flex-1 px-3 py-2 bg-danger-50 text-danger-600 text-xs font-semibold rounded-lg hover:bg-danger-100 border border-danger-500/20 transition-colors cursor-pointer"
              >
                Outage (DOWN)
              </button>
              <button
                onClick={() => triggerAction('degrade', p.id)}
                className="flex-1 px-3 py-2 bg-warning-50 text-warning-600 text-xs font-semibold rounded-lg hover:bg-warning-100 border border-warning-500/20 transition-colors cursor-pointer"
              >
                Degrade
              </button>
              <button
                onClick={() => triggerAction('recover', p.id)}
                className="flex-1 px-3 py-2 bg-success-50 text-success-600 text-xs font-semibold rounded-lg hover:bg-success-100 border border-success-500/20 transition-colors cursor-pointer"
              >
                Recover
              </button>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}

// ============================================================
// SIMULATION LAB PAGE — User 3's primary view
// ============================================================

function SimulationPage() {
  const [logs, setLogs] = useState<string[]>([]);
  const [dropCount, setDropCount] = useState(10);
  const [eventCallId, setEventCallId] = useState('');
  const [eventType, setEventType] = useState('ANSWERED');
  const [crashCallId, setCrashCallId] = useState('');

  const log = (msg: string) => setLogs(prev => [`[${new Date().toLocaleTimeString()}] ${msg}`, ...prev]);

  const runScenario = async (scenario: string) => {
    log(`Running Scenario ${scenario}...`);
    try {
      const res = await axios.post(`${API}/api/simulation/scenario/${scenario}`);
      const pd = res.data.pacingDecision;
      const sd = res.data.safetyDecision;
      log(`Scenario ${scenario} completed. Recommended: ${pd.recommendedCalls}, Approved: ${sd.approvedCalls} (${sd.decision})`);
    } catch {
      log(`Error running scenario ${scenario}`);
    }
  };

  const triggerFailure = async (type: string, provider: string = 'A') => {
    log(`Triggering: ${type} for Provider ${provider}...`);
    try {
      const endpoint = type === 'outage' ? 'provider-outage' : type === 'degrade' ? 'provider-degrade' : 'provider-recover';
      const res = await axios.post(`${API}/api/failure/${endpoint}/${provider}`);
      log(`Result: ${res.data.status}`);
    } catch {
      log(`Error triggering ${type}`);
    }
  };

  const dropAgents = async () => {
    log(`Dropping ${dropCount} agents...`);
    try {
      const res = await axios.post(`${API}/api/simulation/drop-agents?count=${dropCount}`);
      log(`Dropped: ${res.data.droppedAgents}. Remaining available: ${res.data.remainingAvailable}`);
    } catch {
      log('Error dropping agents');
    }
  };

  const restoreAgents = async () => {
    log('Restoring all OFFLINE agents...');
    try {
      const res = await axios.post(`${API}/api/simulation/restore-agents`);
      log(`Restored: ${res.data.restoredAgents}. Total available: ${res.data.totalAvailable}`);
    } catch {
      log('Error restoring agents');
    }
  };

  const injectEvent = async () => {
    if (!eventCallId) { log('Error: Enter a call ID'); return; }
    log(`Injecting event: ${eventType} for call #${eventCallId}...`);
    try {
      const res = await axios.post(`${API}/api/simulation/inject-event?callId=${eventCallId}&eventType=${eventType}`);
      if (res.data.error) {
        log(`Error: ${res.data.error}`);
      } else {
        log(`Event injected. Call #${eventCallId} is now: ${res.data.currentCallState}`);
      }
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Unknown error';
      log(`Error injecting event: ${msg}`);
    }
  };

  const crashWorker = async () => {
    if (!crashCallId) { log('Error: Enter a call ID'); return; }
    log(`Simulating worker crash for call #${crashCallId}...`);
    try {
      const res = await axios.post(`${API}/api/simulation/crash-worker/${crashCallId}`);
      if (res.data.error) {
        log(`Error: ${res.data.error}`);
      } else {
        log(`${res.data.status}. ${res.data.info}`);
      }
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Unknown error';
      log(`Error: ${msg}`);
    }
  };

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h2 className="text-2xl font-bold text-navy-900">Simulation & Failure Lab</h2>
        <p className="text-sm text-navy-500 mt-1">Execute predefined scenarios and inject failures to observe system behavior</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Scenarios */}
        <Card className="p-6">
          <div className="flex items-center gap-2 mb-4">
            <Play className="w-4 h-4 text-success-600" />
            <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide">Scenarios</h3>
          </div>
          <div className="space-y-2">
            {[
              { id: 'A', desc: 'Low answer rate (20%), short talk time (120s)' },
              { id: 'B', desc: 'Medium answer rate (50%), medium talk time (90s)' },
              { id: 'C', desc: 'High answer rate (70%), long talk time (180s)' },
            ].map(s => (
              <button
                key={s.id}
                onClick={() => runScenario(s.id)}
                className="w-full text-left px-4 py-3 bg-navy-50 hover:bg-navy-100 rounded-lg border border-navy-200 transition-colors cursor-pointer"
              >
                <p className="text-sm font-semibold text-navy-900">Scenario {s.id}</p>
                <p className="text-xs text-navy-500">{s.desc}</p>
              </button>
            ))}
          </div>
        </Card>

        {/* Failure injection */}
        <Card className="p-6 border-danger-500/20">
          <div className="flex items-center gap-2 mb-4">
            <ServerCrash className="w-4 h-4 text-danger-600" />
            <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide">Failure Injection</h3>
          </div>
          <div className="space-y-2">
            <button onClick={() => triggerFailure('outage', 'A')}
              className="w-full px-4 py-3 bg-danger-50 hover:bg-danger-100 text-danger-600 font-medium rounded-lg border border-danger-500/20 flex items-center justify-between text-sm cursor-pointer transition-colors">
              Provider A Outage <AlertTriangle className="w-4 h-4" />
            </button>
            <button onClick={() => triggerFailure('degrade', 'A')}
              className="w-full px-4 py-3 bg-warning-50 hover:bg-warning-100 text-warning-600 font-medium rounded-lg border border-warning-500/20 flex items-center justify-between text-sm cursor-pointer transition-colors">
              Provider A Degrade <AlertTriangle className="w-4 h-4" />
            </button>
            <button onClick={() => triggerFailure('recover', 'A')}
              className="w-full px-4 py-3 bg-success-50 hover:bg-success-100 text-success-600 font-medium rounded-lg border border-success-500/20 flex items-center justify-between text-sm cursor-pointer transition-colors">
              Provider A Recover <CheckCircle2 className="w-4 h-4" />
            </button>
          </div>
        </Card>
      </div>

      {/* Agent Drop / Restore */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card className="p-6">
          <div className="flex items-center gap-2 mb-4">
            <UserMinus className="w-4 h-4 text-danger-600" />
            <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide">Drop Agents</h3>
          </div>
          <div className="flex gap-2 items-center">
            <input
              type="number"
              value={dropCount}
              onChange={(e) => setDropCount(Number(e.target.value))}
              className="w-20 px-3 py-2 border border-navy-200 rounded-lg text-sm text-navy-900"
              min={1}
              max={20}
            />
            <button onClick={dropAgents}
              className="flex-1 px-4 py-2 bg-danger-50 hover:bg-danger-100 text-danger-600 text-sm font-semibold rounded-lg border border-danger-500/20 cursor-pointer transition-colors flex items-center justify-center gap-2">
              <UserMinus className="w-4 h-4" /> Drop Agents
            </button>
          </div>
        </Card>
        <Card className="p-6">
          <div className="flex items-center gap-2 mb-4">
            <UserPlus className="w-4 h-4 text-success-600" />
            <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide">Restore Agents</h3>
          </div>
          <button onClick={restoreAgents}
            className="w-full px-4 py-2 bg-success-50 hover:bg-success-100 text-success-600 text-sm font-semibold rounded-lg border border-success-500/20 cursor-pointer transition-colors flex items-center justify-center gap-2">
            <UserPlus className="w-4 h-4" /> Restore All OFFLINE Agents
          </button>
        </Card>
      </div>

      {/* Event Injection */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card className="p-6">
          <div className="flex items-center gap-2 mb-4">
            <PhoneIncoming className="w-4 h-4 text-brand-600" />
            <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide">Inject Provider Event</h3>
          </div>
          <div className="space-y-2">
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Call ID"
                value={eventCallId}
                onChange={(e) => setEventCallId(e.target.value)}
                className="w-24 px-3 py-2 border border-navy-200 rounded-lg text-sm text-navy-900"
              />
              <select
                value={eventType}
                onChange={(e) => setEventType(e.target.value)}
                className="flex-1 px-3 py-2 border border-navy-200 rounded-lg text-sm text-navy-900"
              >
                <option value="INITIATED">INITIATED</option>
                <option value="RINGING">RINGING</option>
                <option value="ANSWERED">ANSWERED</option>
                <option value="CONNECTED">CONNECTED</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="FAILED">FAILED</option>
              </select>
            </div>
            <button onClick={injectEvent}
              className="w-full px-4 py-2 bg-brand-50 hover:bg-brand-100 text-brand-600 text-sm font-semibold rounded-lg border border-brand-500/20 cursor-pointer transition-colors">
              Inject Event
            </button>
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center gap-2 mb-4">
            <PhoneOff className="w-4 h-4 text-danger-600" />
            <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide">Simulate Worker Crash</h3>
          </div>
          <div className="space-y-2">
            <input
              type="text"
              placeholder="Call ID"
              value={crashCallId}
              onChange={(e) => setCrashCallId(e.target.value)}
              className="w-full px-3 py-2 border border-navy-200 rounded-lg text-sm text-navy-900"
            />
            <button onClick={crashWorker}
              className="w-full px-4 py-2 bg-danger-50 hover:bg-danger-100 text-danger-600 text-sm font-semibold rounded-lg border border-danger-500/20 cursor-pointer transition-colors flex items-center justify-center gap-2">
              <ServerCrash className="w-4 h-4" /> Crash Worker
            </button>
          </div>
        </Card>
      </div>

      {/* Console */}
      <Card className="overflow-hidden">
        <div className="flex items-center justify-between px-4 py-2.5 bg-navy-900 border-b border-white/10">
          <span className="text-xs font-semibold text-navy-400 uppercase tracking-wide">Event Log</span>
          <button onClick={() => setLogs([])} className="text-xs text-navy-500 hover:text-navy-300 cursor-pointer">Clear</button>
        </div>
        <div className="bg-navy-900 p-4 min-h-48 max-h-72 overflow-y-auto font-mono text-xs">
          {logs.map((l, i) => (
            <div key={i} className="text-success-500 mb-1">&gt; {l}</div>
          ))}
          {logs.length === 0 && <div className="text-navy-500 italic">No events yet. Run a scenario or inject a failure.</div>}
        </div>
      </Card>
    </div>
  );
}

// ============================================================
// HISTORICAL ANALYTICS PAGE
// ============================================================

function AnalyticsPage() {
  const [stats, setStats] = useState<AnalyticsStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axios.get(`${API}/api/analytics/historical`)
      .then(r => { setStats(r.data); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  if (loading) {
    return <div className="flex items-center justify-center h-64"><Loader2 className="w-8 h-8 text-brand-500 animate-spin" /></div>;
  }

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h2 className="text-2xl font-bold text-navy-900">Historical Analytics Engine</h2>
        <p className="text-sm text-navy-500 mt-1 max-w-3xl">
          The provided 30k collections dataset is ingested on backend startup and serves as the historical foundation for the Predictive Pacing Engine.
        </p>
      </div>

      {/* Metric cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <KPICard
          icon={<Database className="w-5 h-5" />}
          title="Data Source"
          value={stats?.dataSource || '-'}
          subtitle="Parsed by DataIngestionService"
          accent="brand"
        />
        <KPICard
          icon={<Activity className="w-5 h-5" />}
          title="Est. Answer Rate"
          value={stats?.estimatedAnswerRate || '-'}
          subtitle={`From ${stats?.totalHistoricalCalls || '-'} calls`}
          accent="warning"
        />
        <KPICard
          icon={<Clock className="w-5 h-5" />}
          title="Avg Talk Time"
          value={stats?.averageTalkTime || '-'}
          subtitle="Aggregated from connected events"
          accent="success"
        />
        <KPICard
          icon={<Radio className="w-5 h-5" />}
          title="Provider Performance"
          value={stats?.recentAnswerRate || '-'}
          subtitle={stats?.providerPerformance || '-'}
          accent="brand"
        />
      </div>

      {/* Architecture flow */}
      <Card className="p-6">
        <h3 className="text-sm font-bold text-navy-900 uppercase tracking-wide mb-6">Decision Pipeline Architecture</h3>
        <div className="flex flex-col md:flex-row items-center justify-center gap-3 bg-navy-900 text-white p-8 rounded-lg">
          {[
            { icon: <Database className="w-6 h-6 text-blue-400" />, label: '30k Dataset', sub: 'CSVs Ingested' },
            { icon: <Activity className="w-6 h-6 text-indigo-400" />, label: 'Historical Metrics', sub: 'Aggregations' },
            { icon: <Zap className="w-6 h-6 text-amber-400" />, label: 'Predictive Pacing', sub: 'Calculates Output' },
            { icon: <ShieldCheck className="w-6 h-6 text-green-400" />, label: 'Safety Controller', sub: 'Strict Enforcement' },
          ].map((step, i) => (
            <div key={i} className="flex items-center gap-3">
              <div className="flex flex-col items-center justify-center text-center p-4 bg-navy-800 rounded-lg border border-navy-700 w-40 h-28">
                {step.icon}
                <span className="font-semibold text-xs mt-2">{step.label}</span>
                <span className="text-[10px] text-navy-400">{step.sub}</span>
              </div>
              {i < 3 && <ArrowRight className="w-5 h-5 text-navy-500 hidden md:block shrink-0" />}
            </div>
          ))}
        </div>
        <p className="text-xs text-navy-500 text-center mt-4">
          The dataset securely informs the backend pacing logic, which is then strictly bounded by the independent Safety Controller before any provider dials occur.
        </p>
      </Card>
    </div>
  );
}

export default App;
