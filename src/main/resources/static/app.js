/**
 * LibraCore - Modern SaaS Library Management System
 * Author: Abraham Grace F
 * Client Application Logic & API Integration
 */

const API_BASE = '';

// Global Application State
const state = {
  currentRole: 'librarian', // 'librarian' or 'student'
  currentPage: 'dashboard',
  selectedStudentId: null,
  books: [],
  members: [],
  borrowRecords: [],
  fines: [],
  stats: null,
  devops: null,
  charts: {}
};

// Initialize Application
document.addEventListener('DOMContentLoaded', () => {
  initApp();
});

async function initApp() {
  await refreshAllData();
  initCharts();
}

async function refreshAllData() {
  try {
    await Promise.all([
      fetchStats(),
      fetchBooks(),
      fetchMembers(),
      fetchBorrowRecords(),
      fetchFines(),
      fetchDevOpsStatus()
    ]);
    renderCurrentPage();
  } catch (err) {
    console.error('Error refreshing app data:', err);
  }
}

// --- API FETCHERS ---
async function fetchStats() {
  try {
    const res = await fetch(`${API_BASE}/api/stats`);
    if (res.ok) {
      state.stats = await res.json();
      renderStatsCards();
    }
  } catch (e) { }
}

async function fetchBooks() {
  try {
    const res = await fetch(`${API_BASE}/api/books`);
    if (res.ok) {
      state.books = await res.json();
      renderBooksTable();
      renderStudentCatalog();
      populateBookDropdowns();
      updatePopularBooks();
    }
  } catch (e) { }
}

async function fetchMembers() {
  try {
    const res = await fetch(`${API_BASE}/api/members`);
    if (res.ok) {
      state.members = await res.json();
      renderMembersTable();
      populateMemberDropdowns();
    }
  } catch (e) { }
}

async function fetchBorrowRecords() {
  try {
    const res = await fetch(`${API_BASE}/api/borrowing`);
    if (res.ok) {
      state.borrowRecords = await res.json();
      renderRecentBorrowings();
      renderBorrowingTable();
      renderReturnsTable();
      populateReturnDropdown();
    }
  } catch (e) { }
}

async function fetchFines() {
  try {
    const res = await fetch(`${API_BASE}/api/fines`);
    if (res.ok) {
      state.fines = await res.json();
      renderFinesTable();
    }
  } catch (e) { }
}

async function fetchDevOpsStatus() {
  try {
    const res = await fetch(`${API_BASE}/api/stats/devops`);
    if (res.ok) {
      state.devops = await res.json();
      renderSettingsTelemetry();
    }
  } catch (e) { }
}

// --- PAGE & ROLE SWITCHERS ---
function switchPage(pageId) {
  state.currentPage = pageId;
  
  // Highlight active nav item
  document.querySelectorAll('.sidebar-nav .nav-item').forEach(item => {
    item.classList.remove('active');
  });
  const activeNav = document.getElementById(`nav-${pageId}`);
  if (activeNav) activeNav.classList.add('active');

  // Show target page view
  document.querySelectorAll('#librarian-view-container .page-view').forEach(view => {
    view.style.display = 'none';
    view.classList.remove('active');
  });

  const targetView = document.getElementById(`view-${pageId}`);
  if (targetView) {
    targetView.style.display = 'block';
    targetView.classList.add('active');
  }

  renderCurrentPage();
}

function toggleRoleView(role) {
  state.currentRole = role;
  const libContainer = document.getElementById('librarian-view-container');
  const stuContainer = document.getElementById('student-view-container');
  const greetingTitle = document.getElementById('header-greeting-title');
  const greetingSub = document.getElementById('header-greeting-sub');

  if (role === 'student') {
    libContainer.style.display = 'none';
    stuContainer.style.display = 'block';
    greetingTitle.textContent = 'Good morning, Student 👋';
    greetingSub.textContent = 'Browse books, check availability, and manage your loans.';
    renderStudentCatalog();
  } else {
    libContainer.style.display = 'block';
    stuContainer.style.display = 'none';
    greetingTitle.textContent = 'Good morning, Librarian 👋';
    greetingSub.textContent = "Here's what's happening in your library today.";
    switchPage(state.currentPage || 'dashboard');
  }
}

function renderCurrentPage() {
  renderStatsCards();
  renderRecentBorrowings();
  renderBooksTable();
  renderMembersTable();
  renderBorrowingTable();
  renderReturnsTable();
  renderFinesTable();
  updateChartsData();
}

// --- RENDERERS ---
function renderStatsCards() {
  if (!state.stats) return;
  const s = state.stats;
  const setVal = (id, val) => {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
  };

  setVal('stat-total-books', s.totalBooks || 0);
  setVal('stat-issued-books', s.issuedBooks || 0);
  setVal('stat-active-members', s.activeMembers || 0);
  setVal('stat-overdue-books', s.overdueCount || 0);
}

function renderRecentBorrowings() {
  const tbody = document.getElementById('recent-borrowings-tbody');
  if (!tbody) return;

  const records = state.borrowRecords.slice(0, 5);
  if (records.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color: var(--text-muted); padding: 20px;">No recent borrowing records found.</td></tr>`;
    return;
  }

  tbody.innerHTML = records.map(r => `
    <tr>
      <td>
        <div class="book-cell">
          <img src="${r.bookCoverUrl || 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=100'}" class="book-thumb" alt="cover">
          <div>
            <div class="book-title-cell">${escapeHtml(r.bookTitle)}</div>
            <div class="book-author-cell">ISBN: ${escapeHtml(r.isbn || 'N/A')}</div>
          </div>
        </div>
      </td>
      <td><strong>${escapeHtml(r.memberName)}</strong></td>
      <td>${r.dueDate || 'N/A'}</td>
      <td><span class="badge ${r.status === 'ISSUED' ? 'badge-info' : r.status === 'RETURNED' ? 'badge-success' : 'badge-danger'}">${r.status}</span></td>
      <td><button class="btn btn-secondary btn-sm" onclick="showToast('Viewing details for record #${r.id}')">View</button></td>
    </tr>
  `).join('');
}

function updatePopularBooks() {
  const list = document.getElementById('popular-books-list');
  if (!list) return;

  const books = state.books.slice(0, 4);
  if (books.length === 0) {
    list.innerHTML = `<div style="text-align:center; color:var(--text-muted); padding:10px;">No books found.</div>`;
    return;
  }

  list.innerHTML = books.map((b, i) => `
    <div class="popular-item">
      <div class="rank-number">#${i + 1}</div>
      <img src="${b.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=100'}" class="book-thumb" alt="cover">
      <div style="flex:1;">
        <div class="book-title-cell">${escapeHtml(b.title)}</div>
        <div class="book-author-cell">${escapeHtml(b.author)}</div>
      </div>
      <span class="badge badge-secondary">${b.totalQuantity - b.availableQuantity + 3} borrows</span>
    </div>
  `).join('');
}

function renderBooksTable() {
  const tbody = document.getElementById('books-page-tbody');
  if (!tbody) return;

  const books = state.books;
  if (books.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color: var(--text-muted); padding: 20px;">No books available in library.</td></tr>`;
    return;
  }

  tbody.innerHTML = books.map(b => `
    <tr>
      <td><img src="${b.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=100'}" class="book-thumb" alt="cover"></td>
      <td><strong class="book-title-cell">${escapeHtml(b.title)}</strong></td>
      <td>${escapeHtml(b.author)}</td>
      <td><code>${escapeHtml(b.isbn)}</code></td>
      <td><span class="badge badge-secondary">${escapeHtml(b.category)}</span></td>
      <td>${b.totalQuantity}</td>
      <td><span class="badge ${b.availableQuantity > 0 ? 'badge-success' : 'badge-danger'}">${b.availableQuantity} available</span></td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="openIssueBookModal(${b.id})"><i class="fas fa-hand-holding-book"></i> Issue</button>
      </td>
    </tr>
  `).join('');
}

function renderMembersTable() {
  const tbody = document.getElementById('members-page-tbody');
  if (!tbody) return;

  const members = state.members;
  if (members.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-muted); padding: 20px;">No registered members.</td></tr>`;
    return;
  }

  tbody.innerHTML = members.map(m => `
    <tr>
      <td><code>${escapeHtml(m.memberCode)}</code></td>
      <td><strong>${escapeHtml(m.name)}</strong></td>
      <td>${escapeHtml(m.email)}</td>
      <td>${escapeHtml(m.phone || 'N/A')}</td>
      <td><span class="badge ${m.role === 'LIBRARIAN' ? 'badge-info' : 'badge-secondary'}">${m.role}</span></td>
      <td><span class="badge badge-success">${m.status}</span></td>
    </tr>
  `).join('');
}

function renderBorrowingTable() {
  const tbody = document.getElementById('borrowing-page-tbody');
  if (!tbody) return;

  const records = state.borrowRecords;
  if (records.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted); padding: 20px;">No borrow records found.</td></tr>`;
    return;
  }

  tbody.innerHTML = records.map(r => `
    <tr>
      <td>#${r.id}</td>
      <td><strong>${escapeHtml(r.memberName)}</strong></td>
      <td>${escapeHtml(r.bookTitle)}</td>
      <td>${r.issueDate}</td>
      <td>${r.dueDate}</td>
      <td><span class="badge ${r.status === 'ISSUED' ? 'badge-info' : r.status === 'RETURNED' ? 'badge-success' : 'badge-danger'}">${r.status}</span></td>
      <td>
        ${r.status === 'ISSUED' ? `<button class="btn btn-success btn-sm" onclick="quickReturnBook(${r.id})">Return</button>` : `<span class="text-muted">-</span>`}
      </td>
    </tr>
  `).join('');
}

function renderReturnsTable() {
  const tbody = document.getElementById('returns-page-tbody');
  if (!tbody) return;

  const activeRecords = state.borrowRecords.filter(r => r.status === 'ISSUED');
  if (activeRecords.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color: var(--text-muted); padding: 20px;">No active issued books to return.</td></tr>`;
    return;
  }

  tbody.innerHTML = activeRecords.map(r => `
    <tr>
      <td>#${r.id}</td>
      <td><strong>${escapeHtml(r.bookTitle)}</strong></td>
      <td>${escapeHtml(r.memberName)}</td>
      <td>${r.dueDate}</td>
      <td><button class="btn btn-success btn-sm" onclick="quickReturnBook(${r.id})"><i class="fas fa-undo"></i> Confirm Return</button></td>
    </tr>
  `).join('');
}

function renderFinesTable() {
  const tbody = document.getElementById('fines-page-tbody');
  if (!tbody) return;

  const fines = state.fines;
  if (fines.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-muted); padding: 20px;">No fine records.</td></tr>`;
    return;
  }

  tbody.innerHTML = fines.map(f => `
    <tr>
      <td>#${f.id}</td>
      <td><strong>${escapeHtml(f.memberName)}</strong></td>
      <td>${escapeHtml(f.reason || 'Overdue return')}</td>
      <td><strong>$${parseFloat(f.amount).toFixed(2)}</strong></td>
      <td><span class="badge ${f.status === 'PAID' ? 'badge-success' : 'badge-danger'}">${f.status}</span></td>
      <td>
        ${f.status === 'UNPAID' ? `<button class="btn btn-primary btn-sm" onclick="payFine(${f.id})">Mark Paid</button>` : `<span class="badge badge-success">Paid</span>`}
      </td>
    </tr>
  `).join('');
}

function renderStudentCatalog() {
  const grid = document.getElementById('student-catalog-grid');
  const select = document.getElementById('student-select-dropdown');
  if (!grid) return;

  // Populate Student Dropdown
  const studentMembers = state.members.filter(m => m.role === 'STUDENT');
  if (select) {
    select.innerHTML = studentMembers.map(s => `<option value="${s.id}">${s.memberCode} - ${s.name}</option>`).join('');
    if (studentMembers.length > 0 && !state.selectedStudentId) {
      state.selectedStudentId = studentMembers[0].id;
    }
  }

  const books = state.books;
  if (books.length === 0) {
    grid.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color:var(--text-muted); padding: 40px;">No books available in catalog.</div>`;
    return;
  }

  grid.innerHTML = books.map(b => `
    <div class="book-card">
      <img src="${b.coverImageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300'}" class="book-card-cover" alt="cover">
      <div class="book-card-title">${escapeHtml(b.title)}</div>
      <div class="book-card-author">${escapeHtml(b.author)}</div>
      <div class="book-card-footer">
        <span class="badge ${b.availableQuantity > 0 ? 'badge-success' : 'badge-danger'}">${b.availableQuantity > 0 ? 'Available' : 'Out of Stock'}</span>
        <button class="btn btn-primary btn-sm" ${b.availableQuantity === 0 ? 'disabled' : ''} onclick="studentBorrowBook(${b.id})">Borrow</button>
      </div>
    </div>
  `).join('');
}

function renderSettingsTelemetry() {
  const info = document.getElementById('settings-telemetry-info');
  if (!info || !state.devops) return;

  const d = state.devops;
  info.innerHTML = `
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px;">
      <div style="background: var(--bg-main); padding: 14px; border-radius: 8px;">
        <div style="font-size: 0.78rem; color: var(--text-muted);">Database Status</div>
        <div style="font-weight: 700; color: var(--success);">${d.databaseStatus || 'Connected'}</div>
      </div>
      <div style="background: var(--bg-main); padding: 14px; border-radius: 8px;">
        <div style="font-size: 0.78rem; color: var(--text-muted);">Database Engine</div>
        <div style="font-weight: 700;">${d.databaseEngine || 'PostgreSQL'}</div>
      </div>
      <div style="background: var(--bg-main); padding: 14px; border-radius: 8px;">
        <div style="font-size: 0.78rem; color: var(--text-muted);">Application Runtime</div>
        <div style="font-weight: 700;">${d.javaVersion || 'Java 21'}</div>
      </div>
      <div style="background: var(--bg-main); padding: 14px; border-radius: 8px;">
        <div style="font-size: 0.78rem; color: var(--text-muted);">Server Port</div>
        <div style="font-weight: 700;">${d.serverPort || '8085'}</div>
      </div>
    </div>
  `;
}

// --- DROPDOWN POPULATORS & ACTIONS ---
function populateBookDropdowns() {
  const issueBookSelect = document.getElementById('issue-book-id');
  if (issueBookSelect) {
    const availableBooks = state.books.filter(b => b.availableQuantity > 0);
    issueBookSelect.innerHTML = availableBooks.map(b => `<option value="${b.id}">${b.title} (${b.availableQuantity} available)</option>`).join('');
  }
}

function populateMemberDropdowns() {
  const issueMemberSelect = document.getElementById('issue-member-id');
  if (issueMemberSelect) {
    issueMemberSelect.innerHTML = state.members.map(m => `<option value="${m.id}">${m.memberCode} - ${m.name} (${m.role})</option>`).join('');
  }
}

function populateReturnDropdown() {
  const returnSelect = document.getElementById('return-record-id');
  if (returnSelect) {
    const activeRecords = state.borrowRecords.filter(r => r.status === 'ISSUED');
    returnSelect.innerHTML = activeRecords.map(r => `<option value="${r.id}">Record #${r.id} - ${r.bookTitle} (Issued to: ${r.memberName})</option>`).join('');
  }
}

// --- FORM SUBMIT HANDLERS ---
async function handleBookSubmit(e) {
  e.preventDefault();
  const title = document.getElementById('book-title').value;
  const author = document.getElementById('book-author').value;
  const isbn = document.getElementById('book-isbn').value;
  const category = document.getElementById('book-category').value;
  const totalQuantity = parseInt(document.getElementById('book-quantity').value);
  const coverImageUrl = document.getElementById('book-cover').value || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300';

  try {
    const res = await fetch(`${API_BASE}/api/books`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, author, isbn, category, totalQuantity, availableQuantity: totalQuantity, coverImageUrl })
    });
    if (res.ok) {
      showToast('Book added successfully!');
      closeModal('modal-add-book');
      document.getElementById('form-add-book').reset();
      await refreshAllData();
    } else {
      const err = await res.json();
      showToast(err.message || 'Failed to add book', 'danger');
    }
  } catch (err) {
    showToast('Network error while adding book', 'danger');
  }
}

async function handleMemberSubmit(e) {
  e.preventDefault();
  const memberCode = document.getElementById('member-code').value;
  const name = document.getElementById('member-name').value;
  const email = document.getElementById('member-email').value;
  const phone = document.getElementById('member-phone').value;
  const role = document.getElementById('member-role').value;

  try {
    const res = await fetch(`${API_BASE}/api/members`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ memberCode, name, email, phone, role, status: 'ACTIVE' })
    });
    if (res.ok) {
      showToast('Member registered successfully!');
      closeModal('modal-add-member');
      document.getElementById('form-add-member').reset();
      await refreshAllData();
    } else {
      const err = await res.json();
      showToast(err.message || 'Failed to register member', 'danger');
    }
  } catch (err) {
    showToast('Network error while registering member', 'danger');
  }
}

async function handleIssueSubmit(e) {
  e.preventDefault();
  const memberId = parseInt(document.getElementById('issue-member-id').value);
  const bookId = parseInt(document.getElementById('issue-book-id').value);
  const borrowDays = parseInt(document.getElementById('issue-days').value);

  try {
    const res = await fetch(`${API_BASE}/api/borrowing/issue`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ memberId, bookId, borrowDays })
    });
    if (res.ok) {
      showToast('Book issued successfully!');
      closeModal('modal-issue-book');
      await refreshAllData();
    } else {
      const err = await res.json();
      showToast(err.message || 'Failed to issue book', 'danger');
    }
  } catch (err) {
    showToast('Error issuing book', 'danger');
  }
}

async function quickReturnBook(recordId) {
  try {
    const res = await fetch(`${API_BASE}/api/borrowing/return/${recordId}`, { method: 'POST' });
    if (res.ok) {
      showToast('Book returned successfully!');
      await refreshAllData();
    } else {
      showToast('Failed to process return', 'danger');
    }
  } catch (err) {
    showToast('Network error during return', 'danger');
  }
}

async function handleReturnSubmit(e) {
  e.preventDefault();
  const recordId = document.getElementById('return-record-id').value;
  if (!recordId) return;
  await quickReturnBook(recordId);
  closeModal('modal-return-book');
}

async function payFine(fineId) {
  try {
    const res = await fetch(`${API_BASE}/api/fines/${fineId}/pay`, { method: 'POST' });
    if (res.ok) {
      showToast('Fine marked as PAID!');
      await refreshAllData();
    } else {
      showToast('Failed to process fine payment', 'danger');
    }
  } catch (err) {
    showToast('Error processing payment', 'danger');
  }
}

async function studentBorrowBook(bookId) {
  const studentId = state.selectedStudentId || (state.members.find(m => m.role === 'STUDENT') || {}).id;
  if (!studentId) {
    showToast('Please select a student profile first', 'danger');
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/api/borrowing/issue`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ memberId: studentId, bookId: bookId, borrowDays: 14 })
    });
    if (res.ok) {
      showToast('Borrow request completed!');
      await refreshAllData();
    } else {
      const err = await res.json();
      showToast(err.message || 'Borrow request failed', 'danger');
    }
  } catch (err) {
    showToast('Error submitting borrow request', 'danger');
  }
}

// --- MODAL CONTROLS ---
function openAddBookModal() { document.getElementById('modal-add-book').classList.add('active'); }
function openRegisterMemberModal() { document.getElementById('modal-add-member').classList.add('active'); }
function openIssueBookModal(bookId = null) {
  document.getElementById('modal-issue-book').classList.add('active');
  if (bookId) {
    const select = document.getElementById('issue-book-id');
    if (select) select.value = bookId;
  }
}
function openReturnBookModal() { document.getElementById('modal-return-book').classList.add('active'); }
function closeModal(modalId) { document.getElementById(modalId).classList.remove('active'); }

// --- CHARTS INITIALIZER ---
function initCharts() {
  const ctxOverview = document.getElementById('chart-library-overview');
  if (ctxOverview) {
    state.charts.overview = new Chart(ctxOverview, {
      type: 'doughnut',
      data: {
        labels: ['Available', 'Borrowed', 'Overdue'],
        datasets: [{
          data: [21, 5, 1],
          backgroundColor: ['#10b981', '#3b82f6', '#ef4444'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } },
        cutout: '70%'
      }
    });
  }

  const ctxActivity = document.getElementById('chart-borrowing-activity');
  if (ctxActivity) {
    state.charts.activity = new Chart(ctxActivity, {
      type: 'bar',
      data: {
        labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
        datasets: [{
          label: 'Borrowings',
          data: [12, 19, 8, 15, 22, 14, 9],
          backgroundColor: '#6366f1',
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, grid: { borderDash: [4, 4] } }, x: { grid: { display: false } } }
      }
    });
  }
}

function updateChartsData() {
  if (!state.stats || !state.charts.overview) return;
  const s = state.stats;
  state.charts.overview.data.datasets[0].data = [
    s.availableBooks || 0,
    s.issuedBooks || 0,
    s.overdueCount || 0
  ];
  state.charts.overview.update();
}

// --- UTILS & TOASTS ---
function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.style.borderLeft = `4px solid ${type === 'danger' ? '#ef4444' : '#10b981'}`;
  toast.innerHTML = `<i class="fas ${type === 'danger' ? 'fa-exclamation-circle' : 'fa-check-circle'}" style="color:${type === 'danger' ? '#ef4444' : '#10b981'}"></i> ${escapeHtml(message)}`;
  
  container.appendChild(toast);
  setTimeout(() => {
    toast.remove();
  }, 3500);
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str).replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[m]);
}
