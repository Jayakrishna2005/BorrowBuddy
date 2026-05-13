import React, { useState, useEffect } from 'react';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Home({ user }) {
  const [items, setItems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [similarModalItem, setSimilarModalItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [itemsRes, catRes] = await Promise.all([
        fetch(`${API_BASE_URL}/items/`),
        fetch(`${API_BASE_URL}/categories/`)
      ]);
      const itemsData = await itemsRes.json();
      const catData = await catRes.json();
      setItems(itemsData);
      setCategories(catData);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRequest = async (itemId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/requests/`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ item: itemId, borrower: user.id })
      });
      if (response.ok) {
        alert('Item requested successfully!');
      } else {
        const errorData = await response.json();
        alert(`Failed to request item: ${errorData.error || 'Unknown error'}`);
      }
    } catch (err) {
      console.error(err);
      alert('Network error. Failed to request item.');
    }
  };

  const handleDelete = async (itemId) => {
    if (!window.confirm('Are you sure you want to delete this item?')) return;
    
    try {
      const response = await fetch(`${API_BASE_URL}/items/${itemId}/`, {
        method: 'DELETE'
      });
      if (response.ok || response.status === 404) {
        setItems(prevItems => prevItems.filter(i => i.id !== itemId));
        fetchData(); // Ensures everything is perfectly in sync
      } else {
        alert(`Failed to delete item. Status: ${response.status}`);
      }
    } catch (err) {
      console.error(err);
      alert('Network error while deleting item: ' + err.message);
    }
  };

  const filteredItems = items.filter(item => {
    const matchesSearch = item.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          item.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === '' || item.category_name === selectedCategory;
    return matchesSearch && matchesCategory;
  }).sort((a, b) => b.id - a.id);

  return (
    <div className="fade-in">
      {/* Profile Header */}
      <div className="glass-card" style={{ padding: '2rem', marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>Welcome back, {user?.fullName}! 👋</h2>
          <p style={{ color: 'var(--text-muted)' }}>Here are your current stats in the BorrowBuddy ecosystem.</p>
        </div>
        <div style={{ display: 'flex', gap: '1.5rem' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--primary)' }}>{user?.level}</div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Level</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--secondary)' }}>{user?.points}</div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Points</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--success)' }}>{user?.trustScore}</div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Trust Score</div>
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem' }}>
        <h3 style={{ fontSize: '1.25rem' }}>Available Items to Borrow</h3>
        
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <input 
            type="text" 
            placeholder="Search items..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{ flex: '1', minWidth: '250px' }}
          />
          <select 
            value={selectedCategory} 
            onChange={(e) => setSelectedCategory(e.target.value)}
            style={{ width: 'auto', minWidth: '150px' }}
          >
            <option value="">All Categories</option>
            {categories.map(cat => (
              <option key={cat.id} value={cat.name}>{cat.name}</option>
            ))}
          </select>
        </div>
      </div>
      
      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem' }}>
          <div className="loader" style={{ width: '40px', height: '40px' }}></div>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
          {filteredItems.map(item => (
            <div key={item.id} className="glass-card" style={{ display: 'flex', flexDirection: 'column' }}>
              <div style={{ height: '200px', backgroundColor: 'rgba(0,0,0,0.2)', position: 'relative' }}>
                {item.image ? (
                  <img src={item.image.startsWith('http') ? item.image : `http://localhost:8000${item.image}`} alt={item.title} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                ) : (
                  <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>No Image</div>
                )}
                {item.category_name && (
                  <span style={{ position: 'absolute', top: '10px', right: '10px', background: 'var(--primary)', color: 'white', padding: '4px 10px', borderRadius: '20px', fontSize: '0.8rem', fontWeight: 'bold', zIndex: 10 }}>
                    {item.category_name}
                  </span>
                )}
                {!item.is_available && (
                  <div style={{ position: 'absolute', inset: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 5 }}>
                    <span style={{ background: 'var(--danger)', color: 'white', padding: '0.5rem 1rem', borderRadius: '8px', fontWeight: 'bold', letterSpacing: '1px', fontSize: '0.8rem', transform: 'rotate(-5deg)' }}>ALREADY BORROWED</span>
                  </div>
                )}
              </div>
              <div style={{ padding: '1.5rem', flex: 1, display: 'flex', flexDirection: 'column' }}>
                <h4 style={{ fontSize: '1.2rem', marginBottom: '0.5rem' }}>{item.title}</h4>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '0.5rem', flex: 1 }}>{item.description}</p>
                <div style={{ marginBottom: '1rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                  <span style={{ fontSize: '0.8rem', background: 'rgba(255,255,255,0.05)', padding: '2px 8px', borderRadius: '4px', border: '1px solid var(--glass-border)' }}>Limit: {item.max_borrow_days} Days</span>
                  <span style={{ fontSize: '0.8rem', background: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger)', padding: '2px 8px', borderRadius: '4px' }}>Penalty Applies</span>
                </div>
                
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <div style={{ width: '30px', height: '30px', borderRadius: '50%', background: 'linear-gradient(135deg, var(--secondary), var(--primary))' }}></div>
                    <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>{item.owner_name || 'Anonymous'}</span>
                  </div>
                  {user?.id === item.owner_id ? (
                    <button 
                      onClick={() => handleDelete(item.id)}
                      className="btn" 
                      style={{ padding: '0.5rem 1rem', fontSize: '0.9rem', background: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger)', border: '1px solid var(--danger)' }}
                    >
                      Delete
                    </button>
                  ) : item.is_available ? (
                    <button 
                      onClick={() => handleRequest(item.id)}
                      className="btn btn-primary" 
                      style={{ padding: '0.5rem 1rem', fontSize: '0.9rem' }}
                    >
                      Request
                    </button>
                  ) : (
                    <button 
                      onClick={() => setSimilarModalItem(item)}
                      className="btn" 
                      style={{ padding: '0.5rem 1rem', fontSize: '0.9rem', background: 'rgba(0, 209, 178, 0.1)', color: '#00D1B2', border: '1px solid #00D1B2' }}
                    >
                      Find Similar
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
          {filteredItems.length === 0 && (
            <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
              No items available matching your search.
            </div>
          )}
        </div>
      )}

      {similarModalItem && (
        <div className="fade-in" style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.8)', zIndex: 9999, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }} onClick={() => setSimilarModalItem(null)}>
          <div style={{ background: 'var(--glass-bg)', backdropFilter: 'blur(16px)', padding: '2rem', borderRadius: '24px', width: '100%', maxWidth: '600px', border: '1px solid var(--glass-border)' }} onClick={e => e.stopPropagation()}>
            <h3 style={{ marginBottom: '1rem' }}>Similar Available Items</h3>
            <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>Other {similarModalItem.category_name} you might like:</p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', maxHeight: '400px', overflowY: 'auto' }}>
              {items.filter(i => i.is_available && i.category === similarModalItem.category && i.id !== similarModalItem.id).map(similar => (
                <div key={similar.id} style={{ display: 'flex', alignItems: 'center', gap: '1rem', background: 'rgba(0,0,0,0.3)', padding: '1rem', borderRadius: '12px' }}>
                  <img src={similar.image?.startsWith('http') ? similar.image : `http://localhost:8000${similar.image}`} alt={similar.title} style={{ width: '60px', height: '60px', objectFit: 'cover', borderRadius: '8px' }} />
                  <div style={{ flex: 1 }}>
                    <h4 style={{ margin: 0 }}>{similar.title}</h4>
                    <p style={{ margin: 0, fontSize: '0.8rem', color: 'var(--text-muted)' }}>By: {similar.owner_name}</p>
                  </div>
                  <button onClick={() => { setSimilarModalItem(null); handleRequest(similar.id); }} className="btn btn-primary" style={{ padding: '0.5rem 1rem' }}>Request</button>
                </div>
              ))}
              {items.filter(i => i.is_available && i.category === similarModalItem.category && i.id !== similarModalItem.id).length === 0 && (
                <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '2rem 0' }}>No similar items currently available. 😢</p>
              )}
            </div>
            <button onClick={() => setSimilarModalItem(null)} className="btn" style={{ marginTop: '1.5rem', width: '100%', background: 'rgba(255,255,255,0.1)' }}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Home;
