import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import axios from "axios";

const API_URL = import.meta.env.VITE_API_BASE_URL || "/api/expense";
const USER_ID = 1;
const today = () => {
  const date = new Date();
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, 10);
};
const EMPTY_FORM = {
  transactionDate: today(),
  description: "",
  referenceNumber: "",
  withdrawalAmount: "",
  depositAmount: "",
  closingBalance: "",
  category: "UNASSIGNED",
};

function getErrorMessage(error) {
  const response = error.response?.data;
  if (typeof response === "string" && response.trim()) return response;
  if (response?.message) return response.message;
  return error.message || "Something went wrong. Please try again.";
}

function money(value) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(Number(value) || 0);
}

function App() {
  const [expenses, setExpenses] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [file, setFile] = useState(null);
  const uploadInputRef = useRef(null);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");

  const loadExpenses = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await axios.get(`${API_URL}/${USER_ID}`);
      setExpenses(Array.isArray(data) ? data : []);
      setError("");
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadExpenses();
  }, [loadExpenses]);

  const visibleExpenses = useMemo(() => {
    const query = search.trim().toLowerCase();
    return [...expenses]
      .sort((a, b) => new Date(b.transactionDate) - new Date(a.transactionDate))
      .filter((expense) => {
        if (!query) return true;
        return [expense.description, expense.category, expense.referenceNumber]
          .some((value) => String(value || "").toLowerCase().includes(query));
      });
  }, [expenses, search]);

  const totals = useMemo(() => expenses.reduce(
    (result, expense) => ({
      deposits: result.deposits + (Number(expense.depositAmount) || 0),
      withdrawals: result.withdrawals + (Number(expense.withdrawalAmount) || 0),
    }),
    { deposits: 0, withdrawals: 0 },
  ), [expenses]);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function clearForm() {
    setForm({ ...EMPTY_FORM, transactionDate: today() });
    setEditingId(null);
  }

  async function submitEntry(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setNotice("");

    const payload = {
      ...form,
      refernceNumber: form.referenceNumber || null,
      withdrawalAmount: Number(form.withdrawalAmount) || 0,
      depositAmount: Number(form.depositAmount) || 0,
      closingBalance: Number(form.closingBalance) || 0,
      userId: USER_ID,
    };
    delete payload.referenceNumber;
    const body = new URLSearchParams({ payload: JSON.stringify(payload) });

    try {
      const response = editingId
        ? await axios.put(`${API_URL}/${editingId}`, body)
        : await axios.post(`${API_URL}/createEntry`, body);
      setNotice(typeof response.data === "string" ? response.data : "Entry saved.");
      clearForm();
      await loadExpenses();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  }

  function startEditing(expense) {
    setEditingId(expense.id);
    setForm({
      transactionDate: expense.transactionDate || "",
      description: expense.description || "",
      referenceNumber: expense.referenceNumber || "",
      withdrawalAmount: expense.withdrawalAmount ?? "",
      depositAmount: expense.depositAmount ?? "",
      closingBalance: expense.closingBalance ?? "",
      category: expense.category || "UNASSIGNED",
    });
    setNotice("");
    setError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function deleteEntry(id) {
    if (!window.confirm("Delete this expense entry? This cannot be undone.")) return;
    setError("");
    setNotice("");
    try {
      await axios.delete(`${API_URL}/${id}`);
      setNotice(`Entry #${id} deleted.`);
      await loadExpenses();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    }
  }

  async function uploadStatement(event) {
    event.preventDefault();
    if (!file) {
      setError("Choose a PDF statement before uploading.");
      return;
    }
    setUploading(true);
    setError("");
    setNotice("");
    const body = new FormData();
    body.append("file", file);
    body.append("userId", String(USER_ID));

    try {
      const response = await axios.post(`${API_URL}/uploadStatement`, body);
      setNotice(typeof response.data === "string" ? response.data : "Statement uploaded.");
      setFile(null);
      if (uploadInputRef.current) uploadInputRef.current.value = "";
      await loadExpenses();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setUploading(false);
    }
  }

  function chooseStatementFile(candidate) {
    if (!candidate) return;
    if (candidate.type !== "application/pdf" && !candidate.name.toLowerCase().endsWith(".pdf")) {
      setFile(null);
      setError("Choose a PDF file to upload.");
      return;
    }
    setError("");
    setFile(candidate);
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#" aria-label="Ledgerly home">
          <span className="brand-mark">L</span>
          <span>ledgerly</span>
        </a>
        <div className="side-label">WORKSPACE</div>
        <a className="nav-item active" href="#transactions">
          <span className="nav-icon">▦</span> Transactions
        </a>
        <div className="sidebar-bottom">
          <div className="avatar">U</div>
          <div>
            <strong>Default user</strong>
            <span>User ID · {USER_ID}</span>
          </div>
        </div>
      </aside>

      <section className="main-content">
        <header className="topbar">
          <div className="breadcrumb">Workspace <span>/</span> Transactions</div>
          <div className="user-chip"><span className="status-dot" /> Connected as user {USER_ID}</div>
        </header>

        <div className="page-content">
          <section className="page-heading">
            <div>
              <div className="eyebrow">YOUR FINANCES, IN ONE PLACE</div>
              <h1>Transactions</h1>
              <p>Track spending, add entries, or import a bank statement.</p>
            </div>
            <button className="button button-primary heading-button" onClick={() => {
              clearForm();
              document.getElementById("manual-entry")?.scrollIntoView({ behavior: "smooth" });
            }}>
              <span>＋</span> Add transaction
            </button>
          </section>

          <section className="stats-grid" aria-label="Expense summary">
            <article className="stat-card">
              <div className="stat-top"><span>Total transactions</span><span className="stat-icon lavender">▤</span></div>
              <strong>{expenses.length}</strong>
              <small>All recorded entries</small>
            </article>
            <article className="stat-card">
              <div className="stat-top"><span>Total deposits</span><span className="stat-icon mint">↘</span></div>
              <strong>{money(totals.deposits)}</strong>
              <small className="positive">Money received</small>
            </article>
            <article className="stat-card">
              <div className="stat-top"><span>Total withdrawals</span><span className="stat-icon peach">↗</span></div>
              <strong>{money(totals.withdrawals)}</strong>
              <small>Money spent</small>
            </article>
          </section>

          {(error || notice) && (
            <div className={`feedback ${error ? "feedback-error" : "feedback-success"}`} role="status">
              <span>{error ? "!" : "✓"}</span>
              <div>{error || notice}</div>
              <button onClick={() => { setError(""); setNotice(""); }} aria-label="Dismiss message">×</button>
            </div>
          )}

          <section className="forms-grid">
            <article className="panel" id="manual-entry">
              <div className="panel-heading">
                <div className="panel-icon lavender">＋</div>
                <div><h2>{editingId ? "Edit transaction" : "Manual entry"}</h2><p>{editingId ? `Updating entry #${editingId}` : "Add a transaction to your ledger"}</p></div>
              </div>
              <form className="entry-form" onSubmit={submitEntry}>
                <label className="field field-wide">
                  <span>Description <b>*</b></span>
                  <input name="description" value={form.description} onChange={updateField} placeholder="e.g. Grocery shopping" required maxLength={2000} />
                </label>
                <label className="field">
                  <span>Transaction date <b>*</b></span>
                  <input type="date" name="transactionDate" value={form.transactionDate} onChange={updateField} required />
                </label>
                <label className="field">
                  <span>Category</span>
                  <input name="category" value={form.category} onChange={updateField} placeholder="e.g. FOOD" maxLength={50} />
                </label>
                <label className="field field-wide">
                  <span>Reference number</span>
                  <input name="referenceNumber" value={form.referenceNumber} onChange={updateField} placeholder="Optional" />
                </label>
                <label className="field">
                  <span>Withdrawal (₹)</span>
                  <input type="number" name="withdrawalAmount" value={form.withdrawalAmount} onChange={updateField} placeholder="0.00" min="0" step="0.01" />
                </label>
                <label className="field">
                  <span>Deposit (₹)</span>
                  <input type="number" name="depositAmount" value={form.depositAmount} onChange={updateField} placeholder="0.00" min="0" step="0.01" />
                </label>
                <label className="field field-wide">
                  <span>Closing balance (₹)</span>
                  <input type="number" name="closingBalance" value={form.closingBalance} onChange={updateField} placeholder="0.00" step="0.01" />
                </label>
                <div className="form-actions field-wide">
                  {editingId && <button className="button button-quiet" type="button" onClick={clearForm}>Cancel edit</button>}
                  <button className="button button-primary" type="submit" disabled={saving}>
                    {saving ? "Saving…" : editingId ? "Save changes" : "Save transaction"}
                  </button>
                </div>
              </form>
            </article>

            <article className="panel upload-panel">
              <div className="panel-heading">
                <div className="panel-icon mint">↑</div>
                <div><h2>Import statement</h2><p>Upload a bank statement PDF</p></div>
              </div>
              <form onSubmit={uploadStatement}>
                <label
                  className={`drop-zone ${file ? "drop-zone-selected" : ""}`}
                  onDragOver={(event) => event.preventDefault()}
                  onDrop={(event) => {
                    event.preventDefault();
                    chooseStatementFile(event.dataTransfer.files?.[0]);
                  }}
                >
                  <input
                    ref={uploadInputRef}
                    type="file"
                    accept=".pdf,application/pdf"
                    onChange={(event) => chooseStatementFile(event.target.files?.[0])}
                  />
                  <span className="upload-icon">↑</span>
                  <strong>{file ? file.name : "Choose a PDF to upload"}</strong>
                  <span>{file ? `${(file.size / 1024 / 1024).toFixed(2)} MB` : "Select or drag your bank statement here"}</span>
                  <span className="file-format">PDF files only</span>
                </label>
                <div className="upload-note"><span>ⓘ</span> Statement import is currently configured for HDFC Bank PDFs.</div>
                <button className="button button-primary upload-button" type="submit" disabled={uploading || !file}>
                  {uploading ? "Uploading…" : "Upload statement"}
                </button>
              </form>
            </article>
          </section>

          <section className="panel transactions-panel" id="transactions">
            <div className="transactions-heading">
              <div><h2>All transactions</h2><p>Your expense activity for user {USER_ID}</p></div>
              <label className="search-box">
                <span>⌕</span>
                <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search transactions" aria-label="Search transactions" />
              </label>
            </div>
            <div className="table-wrap">
              <table>
                <thead><tr>
                  <th>DESCRIPTION</th><th>DATE</th><th>CATEGORY</th><th>REFERENCE</th><th className="amount-cell">WITHDRAWAL</th><th className="amount-cell">DEPOSIT</th><th className="amount-cell">BALANCE</th><th aria-label="Actions" />
                </tr></thead>
                <tbody>
                  {loading ? (
                    <tr><td className="empty-state" colSpan="8"><span className="spinner" /> Loading transactions…</td></tr>
                  ) : visibleExpenses.length ? visibleExpenses.map((expense) => (
                    <tr key={expense.id}>
                      <td><div className="transaction-description"><span className="transaction-mark">{(expense.description || "T").trim().charAt(0).toUpperCase()}</span><strong>{expense.description || "—"}</strong></div></td>
                      <td>{expense.transactionDate || "—"}</td>
                      <td><span className="category-pill">{expense.category || "UNASSIGNED"}</span></td>
                      <td className="muted-cell">{expense.referenceNumber || "—"}</td>
                      <td className="amount-cell withdrawal">{expense.withdrawalAmount ? money(expense.withdrawalAmount) : "—"}</td>
                      <td className="amount-cell deposit">{expense.depositAmount ? money(expense.depositAmount) : "—"}</td>
                      <td className="amount-cell">{expense.closingBalance != null ? money(expense.closingBalance) : "—"}</td>
                      <td><div className="row-actions">
                        <button className="icon-button" onClick={() => startEditing(expense)} aria-label={`Edit ${expense.description}`} title="Edit">✎</button>
                        <button className="icon-button delete-action" onClick={() => deleteEntry(expense.id)} aria-label={`Delete ${expense.description}`} title="Delete">×</button>
                      </div></td>
                    </tr>
                  )) : (
                    <tr><td className="empty-state" colSpan="8">
                      <span className="empty-icon">▤</span>
                      <strong>{search ? "No matching transactions" : "No transactions yet"}</strong>
                      <span>{search ? "Try another search term." : "Create a manual entry or upload a statement to get started."}</span>
                    </td></tr>
                  )}
                </tbody>
              </table>
            </div>
            <div className="table-footer">Showing <strong>{visibleExpenses.length}</strong> of <strong>{expenses.length}</strong> transactions</div>
          </section>
          <footer className="page-footer">Ledgerly <span>·</span> Expense tracking made simple</footer>
        </div>
      </section>
    </main>
  );
}

export default App;
