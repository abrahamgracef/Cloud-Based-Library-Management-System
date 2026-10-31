async function deleteBook(bookId) {
  if (!confirm('Are you sure you want to delete this book from the library?')) return;
  try {
    const res = await fetch(`${API_BASE}/api/books/${bookId}`, { method: 'DELETE' });
    if (res.ok) {
      showToast('Book deleted successfully!');
      await refreshAllData();
    } else {
      showToast('Failed to delete book', 'danger');
    }
  } catch (err) {
    showToast('Error deleting book', 'danger');
  }
}
