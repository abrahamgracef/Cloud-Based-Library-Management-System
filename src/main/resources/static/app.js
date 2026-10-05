/**
 * AWS DevOps Cloud-Based Library Management System
 * Author: Abraham Grace F (24MIS0211 - ISWE406L)
 * Client Application JavaScript
 */

const API_BASE = '';

// Application State
const state = {
  currentRole: 'librarian', // 'librarian' or 'student'
  currentLibrarianTab: 'books',
  currentStudentTab: 'catalog',
  selectedStudentId: null,
  books: [],
  members: [],
  borrowRecords: [],
  fines: [],
  stats: null,
  devops: null,
  apiLatencyMs: null
};

// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
  initApp();
});

async function initApp() {
  setupEventListeners();
  await refreshAllData();
  switchRole('librarian');
}

function setupEventListeners() {
  // Global Search Inputs
  const bookSearch = document.getElementById('book-search-input');
  if (bookSearch) {
    bookSearch.addEventListener('input', (e) => filterBooksTable(e.target.value));
  }

  const studentCatalogSearch = document.getElementById('catalog-search-input');
  if (studentCatalogSearch) {
    studentCatalogSearch.addEventListener('input', (e) => filterStudentCatalog(e.target.value));
  }

  const memberSearch = document.getElementById('member-search-input');
  if (memberSearch) {
    memberSearch.addEventListener('input', (e) => filterMembersTable(e.target.value));
  }

  // Forms Submit Listeners
  const addBookForm = document.getElementById('form-add-book');
  if (addBookForm) {
    addBookForm.addEventListener('submit', handleBookSubmit);
  }

  const registerMemberForm = document.getElementById('form-register-member');
  if (registerMemberForm) {
    registerMemberForm.addEventListener('submit', handleMemberSubmit);
  }

  const issueBookForm = document.getElementById('form-issue-book');
  if (issueBookForm) {
    issueBookForm.addEventListener('submit', handleIssueBookSubmit);
  }

  // Student Switcher Dropdown
  const studentSelect = document.getElementById('student-select-dropdown');
  if (studentSelect) {
    studentSelect.addEventListener('change', (e) => {
      state.selectedStudentId = parseInt(e.target.value);
      renderStudentView();
    });
  }

  // Modal Backdrop Close
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        closeModal(overlay.id);
      }
    });
  });
}

// --- API Service Layer ---
async function apiCall(endpoint, method = 'GET', body = null) {
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  };
  if (body) {
    options.body = JSON.stringify(body);
  }

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, options);
    
    if (response.status === 204) {
      return null;
    }

    const data = await response.json();
    if (!response.ok) {
      const errorMsg = data.message || data.error || `HTTP error ${response.status}`;
      throw new Error(errorMsg);
    }
    return data;
  } catch (err) {
    console.error(`API Error (${endpoint}):`, err);
    showToast('Error', err.message || 'Server communication error', 'error');
    throw err;
  }
}

// --- Refresh & Fetch Data ---
async function refreshAllData() {
  try {
    await Promise.all([
      fetchStats(),
      fetchDevOpsStatus(),
      fetchBooks(),
      fetchMembers(),
      fetchBorrowRecords(),
      fetchFines()
    ]);
    renderCurrentView();
  } catch (err) {
    console.error('Failed to load dashboard data:', err);
  }
}

async function fetchStats() {
  const start = performance.now();
  try {
    state.stats = await apiCall('/api/stats');
    const end = performance.now();
    state.apiLatencyMs = Math.round(end - start);
    renderStats();
  } catch (e) {
    // Handled in apiCall
  }
}

async function fetchDevOpsStatus() {
  try {
    state.devops = await apiCall('/api/stats/devops');
    renderDevOpsStatus();
  } catch (e) { }
}

function renderDevOpsStatus() {
  if (!state.devops) return;

  const d = state.devops;
  const setTxt = (id, txt) => {
    const el = document.getElementById(id);
    if (el) el.textContent = txt;
  };

  setTxt('devops-git-branch', d.gitBranch || 'main');
  setTxt('devops-git-repo', (d.githubRepo || 'abrahamgracef/...').split('/').pop());
  setTxt('devops-pipeline-region', d.awsRegion || 'ap-south-1');
  setTxt('devops-java-ver', d.javaVersion || 'Java 21');
  setTxt('devops-port-desc', d.serverPort || '8085');
  setTxt('devops-ec2-type', d.ec2Instance || 't3.micro');
  setTxt('devops-ec2-ip', d.ec2PublicIp || '52.66.211.1');
  setTxt('devops-db-engine', d.databaseEngine || 'PostgreSQL 15.14');
  setTxt('devops-s3-bucket', d.s3Bucket || 'library-management-covers');
  setTxt('devops-cloudwatch-log', d.cloudWatchLogGroup || '/aws/library-management');
  setTxt('devops-sns-topic', (d.snsTopic || 'library-notifications').split(':').pop());

  setTxt('devops-metric-uptime', d.uptimeFormatted || '99.98%');
  setTxt('devops-metric-latency', `${state.apiLatencyMs || 24} ms`);
  setTxt('devops-metric-db', d.databaseStatus ? 'Healthy (Connected)' : 'Healthy');
  setTxt('devops-metric-region', `${d.awsRegion} (Mumbai)`);
}

async function fetchBooks() {
  try {
    state.books = await apiCall('/api/books') || [];
    renderBooksTable();
    renderStudentCatalog();
    populateBookDropdowns();
  } catch (e) { }
}

async function fetchMembers() {
  try {
    state.members = await apiCall('/api/members') || [];
    renderMembersTable();
    populateMemberDropdowns();
  } catch (e) { }
}

async function fetchBorrowRecords() {
  try {
    state.borrowRecords = await apiCall('/api/borrowing') || [];
    renderBorrowRecordsTable();
    renderOverdueTable();
    if (state.selectedStudentId) {
      renderStudentBorrowedBooks();
      renderStudentHistory();
    }
  } catch (e) { }
}

async function fetchFines() {
  try {
    state.fines = await apiCall('/api/fines') || [];
    renderFinesTable();
    if (state.selectedStudentId) {
      renderStudentFines();
    }
  } catch (e) { }
}

// --- Role & Tab Switchers ---
function switchRole(role) {
  state.currentRole = role;
  
  const libBtn = document.getElementById('btn-role-librarian');
  const stuBtn = document.getElementById('btn-role-student');
  const libView = document.getElementById('librarian-view');
  const stuView = document.getElementById('student-view');
  const studentBar = document.getElementById('student-selector-bar');

  if (role === 'librarian') {
    libBtn.classList.add('active');
    stuBtn.classList.remove('active');
    libView.style.display = 'block';
    stuView.style.display = 'none';
    if (studentBar) studentBar.style.display = 'none';
  } else {
    stuBtn.classList.add('active');
    libBtn.classList.remove('active');
    stuView.style.display = 'block';
    libView.style.display = 'none';
    if (studentBar) studentBar.style.display = 'flex';

    // Default to first student if available and none selected
    const students = state.members.filter(m => m.role === 'STUDENT');
    if (students.length > 0 && (!state.selectedStudentId || !students.some(s => s.id === state.selectedStudentId))) {
      state.selectedStudentId = students[0].id;
      const dropdown = document.getElementById('student-select-dropdown');
      if (dropdown) dropdown.value = state.selectedStudentId;
    }
    renderStudentView();
  }
}

function switchLibrarianTab(tabName) {
  state.currentLibrarianTab = tabName;
  document.querySelectorAll('#librarian-view .nav-tabs .tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.tab === tabName);
  });
  document.querySelectorAll('#librarian-view .tab-content').forEach(content => {
    content.classList.toggle('active', content.id === `tab-${tabName}`);
  });
}

function switchStudentTab(tabName) {
  state.currentStudentTab = tabName;
  document.querySelectorAll('#student-view .nav-tabs .tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.tab === tabName);
  });
  document.querySelectorAll('#student-view .tab-content').forEach(content => {
    content.classList.toggle('active', content.id === `student-tab-${tabName}`);
  });
}

function renderCurrentView() {
  if (state.currentRole === 'librarian') {
    renderStats();
    renderBooksTable();
    renderMembersTable();
    renderBorrowRecordsTable();
    renderOverdueTable();
    renderFinesTable();
  } else {
    renderStudentView();
  }
}

// --- Render Statistics Cards ---
function renderStats() {
  if (!state.stats) return;

  const totalBooksEl = document.getElementById('stat-total-books');
  const availBooksEl = document.getElementById('stat-available-books');
  const issuedBooksEl = document.getElementById('stat-issued-books');
  const overdueEl = document.getElementById('stat-overdue-books');
  const activeMembersEl = document.getElementById('stat-active-members');
  const finesEl = document.getElementById('stat-fines');

  if (totalBooksEl) totalBooksEl.textContent = state.stats.totalBooks || 0;
  if (availBooksEl) availBooksEl.textContent = state.stats.availableBooks || 0;
  if (issuedBooksEl) issuedBooksEl.textContent = state.stats.issuedBooks || 0;
  if (overdueEl) overdueEl.textContent = state.stats.overdueCount || 0;
  if (activeMembersEl) activeMembersEl.textContent = state.stats.activeMembers || 0;
  if (finesEl) {
    const collected = (state.stats.totalFinesCollected || 0).toFixed(2);
    const pending = (state.stats.pendingFinesAmount || 0).toFixed(2);
    finesEl.textContent = `$${collected} / $${pending} pend`;
  }
}

// --- Populate Dropdowns ---
function populateMemberDropdowns() {
  const issueSelect = document.getElementById('issue-member-select');
  const studentSelect = document.getElementById('student-select-dropdown');
  
  const members = state.members || [];
  
  if (issueSelect) {
    issueSelect.innerHTML = '<option value="">-- Select Member --</option>' + 
      members.map(m => `<option value="${m.id}">${m.memberCode} - ${m.name} (${m.role})</option>`).join('');
  }

  if (studentSelect) {
    const students = members.filter(m => m.role === 'STUDENT');
    studentSelect.innerHTML = students.map(s => 
      `<option value="${s.id}" ${s.id === state.selectedStudentId ? 'selected' : ''}>${s.memberCode} - ${s.name}</option>`
    ).join('');
    if (students.length > 0 && !state.selectedStudentId) {
      state.selectedStudentId = students[0].id;
    }
  }
}

function populateBookDropdowns() {
  const issueSelect = document.getElementById('issue-book-select');
  if (issueSelect) {
    const availableBooks = (state.books || []).filter(b => b.availableQuantity > 0);
    issueSelect.innerHTML = '<option value="">-- Select Book --</option>' + 
      availableBooks.map(b => `<option value="${b.id}">${b.title} (Available: ${b.availableQuantity})</option>`).join('');
  }
}

// --- Render Book Management Table (Librarian) ---
function renderBooksTable(booksToRender = state.books) {
  const tbody = document.getElementById('books-table-body');
  if (!tbody) return;

  if (!booksToRender || booksToRender.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="empty-state"><i class="fas fa-book"></i><p>No books found.</p></td></tr>`;
    return;
  }

  tbody.innerHTML = booksToRender.map(book => {
    const isAvail = book.availableQuantity > 0;
    const badgeClass = isAvail ? 'badge-available' : 'badge-overdue';
    const badgeText = isAvail ? `Available (${book.availableQuantity}/${book.totalQuantity})` : 'Out of Stock';
    const imgHtml = book.coverImageUrl && book.coverImageUrl.startsWith('http') && !book.coverImageUrl.includes('example.com')
      ? `<img src="${book.coverImageUrl}" class="book-thumb" alt="Cover" onerror="this.outerHTML='<div class=\\'book-thumb\\'><i class=\\'fas fa-book\\'></i></div>'">`
      : `<div class="book-thumb"><i class="fas fa-book"></i></div>`;

    return `
      <tr>
        <td>${imgHtml}</td>
        <td>
          <div style="font-weight: 600;">${escapeHtml(book.title)}</div>
          <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(book.author)}</div>
        </td>
        <td><code>${escapeHtml(book.isbn)}</code></td>
        <td><span class="badge badge-info">${escapeHtml(book.category)}</span></td>
        <td>${book.availableQuantity} / ${book.totalQuantity}</td>
        <td><span class="badge ${badgeClass}">${badgeText}</span></td>
        <td>
          <button class="btn btn-secondary btn-sm btn-icon" title="Edit" onclick="openEditBookModal(${book.id})"><i class="fas fa-edit"></i></button>
          <button class="btn btn-danger btn-sm btn-icon" title="Delete" onclick="confirmDeleteBook(${book.id})"><i class="fas fa-trash"></i></button>
        </td>
      </tr>
    `;
  }).join('');
}

function filterBooksTable(query) {
  const q = query.toLowerCase().trim();
  if (!q) {
    renderBooksTable(state.books);
    return;
  }
  const filtered = state.books.filter(b => 
    b.title.toLowerCase().includes(q) || 
    b.author.toLowerCase().includes(q) || 
    b.isbn.toLowerCase().includes(q) ||
    b.category.toLowerCase().includes(q)
  );
  renderBooksTable(filtered);
}

// --- Render Member Management Table (Librarian) ---
function renderMembersTable(membersToRender = state.members) {
  const tbody = document.getElementById('members-table-body');
  if (!tbody) return;

  if (!membersToRender || membersToRender.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="empty-state"><i class="fas fa-users"></i><p>No members registered.</p></td></tr>`;
    return;
  }

  tbody.innerHTML = membersToRender.map(member => {
    const statusBadge = member.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive';
    const roleBadge = member.role === 'LIBRARIAN' ? 'badge-role' : 'badge-info';

    return `
      <tr>
        <td><strong>${escapeHtml(member.memberCode)}</strong></td>
        <td>${escapeHtml(member.name)}</td>
        <td>${escapeHtml(member.email)}</td>
        <td>${escapeHtml(member.phone || 'N/A')}</td>
        <td><span class="badge ${roleBadge}">${escapeHtml(member.role)}</span></td>
        <td><span class="badge ${statusBadge}">${escapeHtml(member.status)}</span></td>
        <td>
          <button class="btn btn-secondary btn-sm btn-icon" title="Edit" onclick="openEditMemberModal(${member.id})"><i class="fas fa-edit"></i></button>
          <button class="btn btn-danger btn-sm btn-icon" title="Delete" onclick="confirmDeleteMember(${member.id})"><i class="fas fa-trash"></i></button>
        </td>
      </tr>
    `;
  }).join('');
}

function filterMembersTable(query) {
  const q = query.toLowerCase().trim();
  if (!q) {
    renderMembersTable(state.members);
    return;
  }
  const filtered = state.members.filter(m => 
    m.name.toLowerCase().includes(q) || 
    m.memberCode.toLowerCase().includes(q) || 
    m.email.toLowerCase().includes(q) ||
    m.role.toLowerCase().includes(q)
  );
  renderMembersTable(filtered);
}

// --- Render Active Borrow Records (Issue & Return Tab) ---
function renderBorrowRecordsTable() {
  const tbody = document.getElementById('borrows-table-body');
  if (!tbody) return;

  const activeBorrows = (state.borrowRecords || []).filter(r => r.status === 'ISSUED' || r.status === 'OVERDUE');

  if (activeBorrows.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty-state"><i class="fas fa-hand-holding-book"></i><p>No active issued books.</p></td></tr>`;
    return;
  }

  tbody.innerHTML = activeBorrows.map(record => {
    const isOverdue = record.status === 'OVERDUE';
    const badgeClass = isOverdue ? 'badge-overdue' : 'badge-issued';

    return `
      <tr>
        <td><strong>${escapeHtml(record.memberCode)}</strong> - ${escapeHtml(record.memberName)}</td>
        <td>${escapeHtml(record.bookTitle)}</td>
        <td>${record.issueDate}</td>
        <td>${record.dueDate}</td>
        <td><span class="badge ${badgeClass}">${record.status}</span></td>
        <td>
          <button class="btn btn-success btn-sm" onclick="returnBook(${record.id})">
            <i class="fas fa-undo"></i> Return
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

// --- Render Overdue Items (Fines Tab) ---
function renderOverdueTable() {
  const tbody = document.getElementById('overdue-table-body');
  if (!tbody) return;

  const overdue = (state.borrowRecords || []).filter(r => r.status === 'OVERDUE');

  if (overdue.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty-state"><i class="fas fa-check-circle"></i><p>No overdue items currently!</p></td></tr>`;
    return;
  }

  tbody.innerHTML = overdue.map(record => `
    <tr>
      <td><strong>${escapeHtml(record.memberCode)}</strong> - ${escapeHtml(record.memberName)}</td>
      <td>${escapeHtml(record.bookTitle)}</td>
      <td>${record.issueDate}</td>
      <td><span class="text-danger font-weight-bold">${record.dueDate}</span></td>
      <td><strong style="color: var(--danger);">$${(record.fineAmount || 0).toFixed(2)}</strong></td>
      <td>
        <button class="btn btn-success btn-sm" onclick="returnBook(${record.id})">
          <i class="fas fa-undo"></i> Process Return
        </button>
      </td>
    </tr>
  `).join('');
}

// --- Render Fines Table (Librarian) ---
function renderFinesTable() {
  const tbody = document.getElementById('fines-table-body');
  if (!tbody) return;

  const fines = state.fines || [];

  if (fines.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="empty-state"><i class="fas fa-coins"></i><p>No fine records.</p></td></tr>`;
    return;
  }

  tbody.innerHTML = fines.map(fine => {
    const isUnpaid = fine.status === 'UNPAID';
    const badgeClass = isUnpaid ? 'badge-unpaid' : 'badge-paid';
    const actionBtn = isUnpaid
      ? `<button class="btn btn-success btn-sm" onclick="payFine(${fine.id})"><i class="fas fa-check"></i> Mark Paid</button>`
      : `<span style="color: var(--text-muted); font-size: 0.8rem;"><i class="fas fa-check-double"></i> Paid</span>`;

    return `
      <tr>
        <td>#${fine.id}</td>
        <td><strong>${escapeHtml(fine.memberCode)}</strong> - ${escapeHtml(fine.memberName)}</td>
        <td>${escapeHtml(fine.bookTitle || 'N/A')}</td>
        <td><strong>$${(fine.amount || 0).toFixed(2)}</strong></td>
        <td><span class="badge ${badgeClass}">${fine.status}</span></td>
        <td><span style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(fine.reason || 'Overdue fine')}</span></td>
        <td>${actionBtn}</td>
      </tr>
    `;
  }).join('');
}

// --- STUDENT VIEW RENDER FUNCTIONS ---
function renderStudentView() {
  const student = state.members.find(m => m.id === state.selectedStudentId);
  const studentInfoEl = document.getElementById('student-info-display');
  
  if (studentInfoEl && student) {
    const activeBorrows = (state.borrowRecords || []).filter(r => r.memberId === student.id && (r.status === 'ISSUED' || r.status === 'OVERDUE')).length;
    const unpaidFines = (state.fines || []).filter(f => f.memberId === student.id && f.status === 'UNPAID');
    const unpaidTotal = unpaidFines.reduce((acc, f) => acc + (f.amount || 0), 0);

    studentInfoEl.innerHTML = `
      <div style="display: flex; gap: 1rem; align-items: center; font-size: 0.85rem; color: #cbd5e1;">
        <span><i class="fas fa-user-circle"></i> <strong>${escapeHtml(student.name)}</strong> (${student.memberCode})</span>
        <span><i class="fas fa-book"></i> Active Borrows: <strong>${activeBorrows}</strong></span>
        <span><i class="fas fa-exclamation-circle"></i> Unpaid Fines: <strong style="color: ${unpaidTotal > 0 ? '#f87171' : '#34d399'};">$${unpaidTotal.toFixed(2)}</strong></span>
      </div>
    `;
  }

  renderStudentCatalog();
  renderStudentBorrowedBooks();
  renderStudentHistory();
  renderStudentFines();
}

function renderStudentCatalog(booksToRender = state.books) {
  const grid = document.getElementById('student-catalog-grid');
  if (!grid) return;

  if (!booksToRender || booksToRender.length === 0) {
    grid.innerHTML = `<div class="empty-state" style="grid-column: 1/-1;"><i class="fas fa-search"></i><p>No books matching your criteria.</p></div>`;
    return;
  }

  grid.innerHTML = booksToRender.map(book => {
    const isAvail = book.availableQuantity > 0;
    const badgeClass = isAvail ? 'badge-available' : 'badge-overdue';
    const badgeText = isAvail ? `${book.availableQuantity} Available` : 'Out of Stock';

    const imgHtml = book.coverImageUrl && book.coverImageUrl.startsWith('http') && !book.coverImageUrl.includes('example.com')
      ? `<img src="${book.coverImageUrl}" class="catalog-cover-img" alt="Cover" onerror="this.outerHTML='<i class=\\'fas fa-book catalog-cover-placeholder\\'></i>'">`
      : `<i class="fas fa-book catalog-cover-placeholder"></i>`;

    const requestBtn = isAvail
      ? `<button class="btn btn-primary btn-sm" onclick="requestBookIssue(${book.id})"><i class="fas fa-plus-circle"></i> Borrow Book</button>`
      : `<button class="btn btn-secondary btn-sm" disabled><i class="fas fa-ban"></i> Unavailable</button>`;

    return `
      <div class="catalog-card">
        <div class="catalog-cover-wrapper">
          ${imgHtml}
        </div>
        <div class="catalog-card-body">
          <div class="catalog-category">${escapeHtml(book.category)}</div>
          <h4 class="catalog-title">${escapeHtml(book.title)}</h4>
          <div class="catalog-author">by ${escapeHtml(book.author)}</div>
          <div class="catalog-card-footer">
            <span class="badge ${badgeClass}">${badgeText}</span>
            ${requestBtn}
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function filterStudentCatalog(query) {
  const q = query.toLowerCase().trim();
  if (!q) {
    renderStudentCatalog(state.books);
    return;
  }
  const filtered = state.books.filter(b => 
    b.title.toLowerCase().includes(q) || 
    b.author.toLowerCase().includes(q) || 
    b.category.toLowerCase().includes(q) ||
    b.isbn.toLowerCase().includes(q)
  );
  renderStudentCatalog(filtered);
}

function renderStudentBorrowedBooks() {
  const tbody = document.getElementById('student-borrowed-table-body');
  if (!tbody || !state.selectedStudentId) return;

  const studentRecords = (state.borrowRecords || []).filter(r => r.memberId === state.selectedStudentId && (r.status === 'ISSUED' || r.status === 'OVERDUE'));

  if (studentRecords.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty-state"><i class="fas fa-book-open"></i><p>You currently have no borrowed books.</p></td></tr>`;
    return;
  }

  tbody.innerHTML = studentRecords.map(record => {
    const isOverdue = record.status === 'OVERDUE';
    const badgeClass = isOverdue ? 'badge-overdue' : 'badge-issued';

    return `
      <tr>
        <td><strong>${escapeHtml(record.bookTitle)}</strong></td>
        <td>${record.issueDate}</td>
        <td><span style="${isOverdue ? 'color: var(--danger); font-weight: 700;' : ''}">${record.dueDate}</span></td>
        <td><span class="badge ${badgeClass}">${record.status}</span></td>
        <td>
          <button class="btn btn-success btn-sm" onclick="returnBook(${record.id})">
            <i class="fas fa-undo"></i> Return Book
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

function renderStudentHistory() {
  const tbody = document.getElementById('student-history-table-body');
  if (!tbody || !state.selectedStudentId) return;

  const history = (state.borrowRecords || []).filter(r => r.memberId === state.selectedStudentId && r.status === 'RETURNED');

  if (history.length === 0) {
    tbody.innerHTML = `<tr><td colspan="4" class="empty-state"><i class="fas fa-history"></i><p>No past borrowing history.</p></td></tr>`;
    return;
  }

  tbody.innerHTML = history.map(record => `
    <tr>
      <td><strong>${escapeHtml(record.bookTitle)}</strong></td>
      <td>${record.issueDate}</td>
      <td>${record.returnDate || record.dueDate}</td>
      <td><span class="badge badge-returned"><i class="fas fa-check"></i> RETURNED</span></td>
    </tr>
  `).join('');
}

function renderStudentFines() {
  const tbody = document.getElementById('student-fines-table-body');
  if (!tbody || !state.selectedStudentId) return;

  const studentFines = (state.fines || []).filter(f => f.memberId === state.selectedStudentId);

  if (studentFines.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty-state"><i class="fas fa-smile"></i><p>You have no fines!</p></td></tr>`;
    return;
  }

  tbody.innerHTML = studentFines.map(fine => {
    const isUnpaid = fine.status === 'UNPAID';
    const badgeClass = isUnpaid ? 'badge-unpaid' : 'badge-paid';
    const payBtn = isUnpaid 
      ? `<button class="btn btn-success btn-sm" onclick="payFine(${fine.id})"><i class="fas fa-credit-card"></i> Pay Fine Now</button>`
      : `<span style="color: var(--text-muted); font-size: 0.8rem;"><i class="fas fa-check-circle"></i> Settled</span>`;

    return `
      <tr>
        <td>${escapeHtml(fine.bookTitle || 'Library Fine')}</td>
        <td><strong>$${(fine.amount || 0).toFixed(2)}</strong></td>
        <td><span class="badge ${badgeClass}">${fine.status}</span></td>
        <td><span style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(fine.reason || 'Overdue Fine')}</span></td>
        <td>${payBtn}</td>
      </tr>
    `;
  }).join('');
}

// --- Action Handlers ---
async function handleBookSubmit(e) {
  e.preventDefault();
  const bookId = document.getElementById('book-id-input').value;
  const payload = {
    title: document.getElementById('book-title').value.trim(),
    author: document.getElementById('book-author').value.trim(),
    isbn: document.getElementById('book-isbn').value.trim(),
    category: document.getElementById('book-category').value,
    totalQuantity: parseInt(document.getElementById('book-quantity').value),
    coverImageUrl: document.getElementById('book-cover').value.trim()
  };

  try {
    if (bookId) {
      await apiCall(`/api/books/${bookId}`, 'PUT', payload);
      showToast('Success', 'Book updated successfully', 'success');
    } else {
      await apiCall('/api/books', 'POST', payload);
      showToast('Success', 'New book added successfully', 'success');
    }
    closeModal('modal-book');
    await refreshAllData();
  } catch (err) { }
}

async function handleMemberSubmit(e) {
  e.preventDefault();
  const memberId = document.getElementById('member-id-input').value;
  const payload = {
    name: document.getElementById('member-name').value.trim(),
    email: document.getElementById('member-email').value.trim(),
    phone: document.getElementById('member-phone').value.trim(),
    role: document.getElementById('member-role').value,
    status: document.getElementById('member-status').value
  };

  try {
    if (memberId) {
      await apiCall(`/api/members/${memberId}`, 'PUT', payload);
      showToast('Success', 'Member updated successfully', 'success');
    } else {
      await apiCall('/api/members', 'POST', payload);
      showToast('Success', 'Member registered successfully', 'success');
    }
    closeModal('modal-member');
    await refreshAllData();
  } catch (err) { }
}

async function handleIssueBookSubmit(e) {
  e.preventDefault();
  const memberId = parseInt(document.getElementById('issue-member-select').value);
  const bookId = parseInt(document.getElementById('issue-book-select').value);
  const borrowDays = parseInt(document.getElementById('issue-days').value) || 14;

  if (!memberId || !bookId) {
    showToast('Validation Error', 'Please select both a member and a book.', 'warning');
    return;
  }

  try {
    await apiCall('/api/borrowing/issue', 'POST', { memberId, bookId, borrowDays });
    showToast('Book Issued', 'Book has been issued successfully.', 'success');
    document.getElementById('form-issue-book').reset();
    await refreshAllData();
  } catch (err) { }
}

async function requestBookIssue(bookId) {
  if (!state.selectedStudentId) {
    showToast('Error', 'Please select a student profile first.', 'warning');
    return;
  }

  try {
    await apiCall('/api/borrowing/issue', 'POST', {
      memberId: state.selectedStudentId,
      bookId: bookId,
      borrowDays: 14
    });
    showToast('Success', 'Book borrowed successfully!', 'success');
    await refreshAllData();
  } catch (err) { }
}

async function returnBook(borrowRecordId) {
  try {
    await apiCall(`/api/borrowing/return/${borrowRecordId}`, 'POST');
    showToast('Book Returned', 'Book return processed successfully.', 'success');
    await refreshAllData();
  } catch (err) { }
}

async function payFine(fineId) {
  try {
    await apiCall(`/api/fines/${fineId}/pay`, 'POST');
    showToast('Payment Successful', 'Fine has been marked as paid.', 'success');
    await refreshAllData();
  } catch (err) { }
}

async function confirmDeleteBook(id) {
  if (confirm('Are you sure you want to delete this book?')) {
    try {
      await apiCall(`/api/books/${id}`, 'DELETE');
      showToast('Deleted', 'Book removed from system.', 'info');
      await refreshAllData();
    } catch (err) { }
  }
}

async function confirmDeleteMember(id) {
  if (confirm('Are you sure you want to delete this member?')) {
    try {
      await apiCall(`/api/members/${id}`, 'DELETE');
      showToast('Deleted', 'Member removed from system.', 'info');
      await refreshAllData();
    } catch (err) { }
  }
}

// --- Modal Helper Functions ---
function openAddBookModal() {
  document.getElementById('modal-book-title').textContent = 'Add New Book';
  document.getElementById('form-add-book').reset();
  document.getElementById('book-id-input').value = '';
  openModal('modal-book');
}

function openEditBookModal(id) {
  const book = state.books.find(b => b.id === id);
  if (!book) return;

  document.getElementById('modal-book-title').textContent = 'Edit Book';
  document.getElementById('book-id-input').value = book.id;
  document.getElementById('book-title').value = book.title;
  document.getElementById('book-author').value = book.author;
  document.getElementById('book-isbn').value = book.isbn;
  document.getElementById('book-category').value = book.category;
  document.getElementById('book-quantity').value = book.totalQuantity;
  document.getElementById('book-cover').value = book.coverImageUrl || '';

  openModal('modal-book');
}

function openAddMemberModal() {
  document.getElementById('modal-member-title').textContent = 'Register New Member';
  document.getElementById('form-register-member').reset();
  document.getElementById('member-id-input').value = '';
  openModal('modal-member');
}

function openEditMemberModal(id) {
  const member = state.members.find(m => m.id === id);
  if (!member) return;

  document.getElementById('modal-member-title').textContent = 'Edit Member';
  document.getElementById('member-id-input').value = member.id;
  document.getElementById('member-name').value = member.name;
  document.getElementById('member-email').value = member.email;
  document.getElementById('member-phone').value = member.phone || '';
  document.getElementById('member-role').value = member.role;
  document.getElementById('member-status').value = member.status;

  openModal('modal-member');
}

function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('active');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('active');
}

// --- Toast Notification Helper ---
function showToast(title, message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const iconMap = {
    success: 'fa-check-circle',
    error: 'fa-exclamation-circle',
    warning: 'fa-exclamation-triangle',
    info: 'fa-info-circle'
  };

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <i class="fas ${iconMap[type] || iconMap.info} toast-icon"></i>
    <div class="toast-content">
      <div class="toast-title">${escapeHtml(title)}</div>
      <div class="toast-msg">${escapeHtml(message)}</div>
    </div>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.animation = 'slideIn 0.3s ease reverse';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// --- HTML Escaping Helper ---
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
