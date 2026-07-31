import React, { useState, useEffect } from 'react';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Home({ user }) {
  const [items, setItems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [similarModalItem, setSimilarModalItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [profileData, setProfileData] = useState(user);
  const [quantityModalItem, setQuantityModalItem] = useState(null);
  const [requestedQuantity, setRequestedQuantity] = useState(1);
  const [selectedDetailItem, setSelectedDetailItem] = useState(null);

  useEffect(() => {
    fetchData();
    if (user?.id) {
      fetchProfile();
    }
  }, [user?.id]);

  const fetchProfile = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/profile/${user.id}/`);
      if (response.ok) {
        const data = await response.json();
        setProfileData(data);
        localStorage.setItem('user', JSON.stringify(data));
      }
    } catch (err) {
      console.error(err);
    }
  };

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

  const handleRequest = async (item, forceQty = null) => {
    let requestQty = 1;
    if (item.quantity > 1 && forceQty === null) {
      setRequestedQuantity(1);
      setQuantityModalItem(item);
      return;
    }
    
    if (forceQty !== null) {
      requestQty = forceQty;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/requests/`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ item: item.id, borrower: user.id, quantity: requestQty })
      });
      if (response.ok) {
        alert('Item requested successfully!');
        if (user?.id) fetchProfile(); // Refresh stats after request
        fetchData(); // Refresh list to get updated quantities
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
        if (user?.id) fetchProfile(); // Refresh stats after delete
      } else {
        alert(`Failed to delete item. Status: ${response.status}`);
      }
    } catch (err) {
      console.error(err);
      alert('Network error while deleting item: ' + err.message);
    }
  };

  const filteredItems = items.filter(item => {
    const matchesSearch = item.title.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === '' || item.category_name === selectedCategory;
    return matchesSearch && matchesCategory;
  });

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
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--primary)' }}>{profileData?.itemsLent || 0}</div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Shared</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--secondary)' }}>{profileData?.itemsBorrowed || 0}</div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Borrowed</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--success)' }}>{profileData?.trustScore || 0}★</div>
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
            <div 
              key={item.id} 
              className="glass-card" 
              style={{ display: 'flex', flexDirection: 'column', cursor: 'pointer', transition: 'transform 0.2s ease, box-shadow 0.2s ease' }}
              onClick={() => setSelectedDetailItem(item)}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateY(-4px)';
                e.currentTarget.style.boxShadow = '0 12px 24px rgba(0, 0, 0, 0.2)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = 'none';
              }}
            >
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
                <div style={{ marginBottom: '1rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap', flex: 1, alignItems: 'flex-start' }}>
                  <span style={{ fontSize: '0.8rem', background: 'rgba(255,255,255,0.05)', padding: '2px 8px', borderRadius: '4px', border: '1px solid var(--glass-border)' }}>Quantity: {item.quantity}</span>
                </div>
                
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }} onClick={(e) => e.stopPropagation()}>
                    <div style={{ width: '30px', height: '30px', borderRadius: '50%', background: 'linear-gradient(135deg, var(--secondary), var(--primary))' }}></div>
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                      <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>By: {item.owner_name || 'Anonymous'}</span>
                      {item.reviews_count > 0 && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.2rem', color: '#FFB400', fontSize: '0.8rem', fontWeight: 'bold', marginTop: '0.1rem' }}>
                          <span>★</span>
                          <span>{item.average_rating} ({item.reviews_count})</span>
                        </div>
                      )}
                    </div>
                  </div>
                  {user?.id === item.owner_id ? (
                    <button 
                      onClick={(e) => { e.stopPropagation(); handleDelete(item.id); }}
                      className="btn" 
                      style={{ padding: '0.5rem 1rem', fontSize: '0.9rem', background: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger)', border: '1px solid var(--danger)' }}
                    >
                      Delete
                    </button>
                  ) : item.is_available ? (
                    <button 
                      onClick={(e) => { e.stopPropagation(); handleRequest(item); }}
                      className="btn btn-primary" 
                      style={{ padding: '0.5rem 1rem', fontSize: '0.9rem' }}
                    >
                      Request
                    </button>
                  ) : (
                    <button 
                      onClick={(e) => { e.stopPropagation(); setSimilarModalItem(item); }}
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

      {quantityModalItem && (
        <div className="fade-in" style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(15, 23, 42, 0.3)', backdropFilter: 'blur(12px)', WebkitBackdropFilter: 'blur(12px)', zIndex: 9999, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }} onClick={() => setQuantityModalItem(null)}>
          <div style={{ background: 'var(--glass-bg)', backdropFilter: 'blur(16px)', padding: '2rem', borderRadius: '24px', width: '100%', maxWidth: '400px', border: '1px solid var(--glass-border)', textAlign: 'center' }} onClick={e => e.stopPropagation()}>
            <h3 style={{ marginBottom: '0.5rem', color: 'var(--text-main)' }}>Select Quantity</h3>
            <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>How many of <strong style={{ color: 'var(--text-main)' }}>{quantityModalItem.title}</strong> do you need?</p>
            
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '1.5rem', marginBottom: '2rem' }}>
              <button 
                onClick={() => setRequestedQuantity(q => Math.max(1, q - 1))}
                className="btn"
                style={{ 
                  width: '40px', 
                  height: '40px', 
                  padding: 0, 
                  borderRadius: '50%', 
                  fontSize: '1.5rem', 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center', 
                  background: requestedQuantity <= 1 ? 'rgba(0,0,0,0.03)' : 'rgba(108, 92, 231, 0.1)', 
                  color: requestedQuantity <= 1 ? 'var(--text-muted)' : 'var(--primary)',
                  cursor: requestedQuantity <= 1 ? 'not-allowed' : 'pointer'
                }}
                disabled={requestedQuantity <= 1}
              >
                -
              </button>
              <span style={{ fontSize: '2rem', fontWeight: 'bold', minWidth: '60px', color: 'var(--text-main)' }}>{requestedQuantity}</span>
              <button 
                onClick={() => setRequestedQuantity(q => Math.min(quantityModalItem.quantity, q + 1))}
                className="btn"
                style={{ 
                  width: '40px', 
                  height: '40px', 
                  padding: 0, 
                  borderRadius: '50%', 
                  fontSize: '1.5rem', 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center', 
                  background: requestedQuantity >= quantityModalItem.quantity ? 'rgba(0,0,0,0.03)' : 'rgba(108, 92, 231, 0.1)', 
                  color: requestedQuantity >= quantityModalItem.quantity ? 'var(--text-muted)' : 'var(--primary)',
                  cursor: requestedQuantity >= quantityModalItem.quantity ? 'not-allowed' : 'pointer'
                }}
                disabled={requestedQuantity >= quantityModalItem.quantity}
              >
                +
              </button>
            </div>
            
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>Available: {quantityModalItem.quantity}</p>

            <div style={{ display: 'flex', gap: '1rem' }}>
              <button 
                onClick={() => setQuantityModalItem(null)} 
                className="btn" 
                style={{ flex: 1, background: 'rgba(0,0,0,0.05)', color: 'var(--text-main)', border: '1px solid rgba(0,0,0,0.1)' }}
              >
                Cancel
              </button>
              <button 
                onClick={() => {
                  const itemToRequest = quantityModalItem;
                  const qty = requestedQuantity;
                  setQuantityModalItem(null);
                  handleRequest(itemToRequest, qty);
                }} 
                className="btn btn-primary" 
                style={{ flex: 1 }}
              >
                Confirm Request
              </button>
            </div>
          </div>
        </div>
      )}

      {similarModalItem && (
        <div className="fade-in" style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(15, 23, 42, 0.3)', backdropFilter: 'blur(12px)', WebkitBackdropFilter: 'blur(12px)', zIndex: 9999, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }} onClick={() => setSimilarModalItem(null)}>
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
                  <button onClick={() => { setSimilarModalItem(null); handleRequest(similar); }} className="btn btn-primary" style={{ padding: '0.5rem 1rem' }}>Request</button>
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

      {selectedDetailItem && (
        <div className="fade-in" style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(15, 23, 42, 0.3)', backdropFilter: 'blur(12px)', WebkitBackdropFilter: 'blur(12px)', zIndex: 9999, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }} onClick={() => setSelectedDetailItem(null)}>
          <div style={{ background: 'var(--glass-bg)', backdropFilter: 'blur(16px)', padding: '2.5rem', borderRadius: '24px', width: '100%', maxWidth: '650px', border: '1px solid var(--glass-border)', display: 'flex', flexDirection: 'column', gap: '1.5rem', maxHeight: '90vh', overflowY: 'auto' }} onClick={e => e.stopPropagation()}>
            
            {/* Header / Title */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <h2 style={{ fontSize: '1.8rem', color: 'var(--text-main)', marginBottom: '0.25rem' }}>{selectedDetailItem.title}</h2>
                <span style={{ background: 'var(--primary)', color: 'white', padding: '4px 12px', borderRadius: '20px', fontSize: '0.8rem', fontWeight: 'bold' }}>
                  {selectedDetailItem.category_name}
                </span>
              </div>
              <button 
                onClick={() => setSelectedDetailItem(null)} 
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', fontSize: '1.5rem', cursor: 'pointer', padding: 0 }}
              >
                &times;
              </button>
            </div>

            {/* Image */}
            <div style={{ height: '250px', borderRadius: '16px', overflow: 'hidden', position: 'relative' }}>
              {selectedDetailItem.image ? (
                <img src={selectedDetailItem.image.startsWith('http') ? selectedDetailItem.image : `http://localhost:8000${selectedDetailItem.image}`} alt={selectedDetailItem.title} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              ) : (
                <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(255,255,255,0.05)', color: 'var(--text-muted)' }}>No Image Available</div>
              )}
            </div>

            {/* Details Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', background: 'rgba(0,0,0,0.02)', padding: '1rem', borderRadius: '12px', border: '1px solid var(--glass-border)' }}>
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Owner</span>
                <p style={{ margin: 0, fontWeight: 'bold', color: 'var(--text-main)' }}>{selectedDetailItem.owner_name}</p>
              </div>
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Status</span>
                <p style={{ margin: 0, fontWeight: 'bold', color: selectedDetailItem.is_available ? 'var(--success)' : 'var(--danger)' }}>
                  {selectedDetailItem.is_available ? 'Available' : 'Already Borrowed'}
                </p>
              </div>
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Available Quantity</span>
                <p style={{ margin: 0, fontWeight: 'bold', color: 'var(--text-main)' }}>{selectedDetailItem.quantity}</p>
              </div>
            </div>

            {/* Description */}
            {selectedDetailItem.description && (
              <div>
                <h4 style={{ fontSize: '1rem', color: 'var(--text-main)', marginBottom: '0.5rem' }}>Description</h4>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', margin: 0, lineHeight: '1.5' }}>{selectedDetailItem.description}</p>
              </div>
            )}

            {/* Reviews Section */}
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', borderBottom: '1px solid var(--glass-border)', paddingBottom: '0.5rem' }}>
                <h4 style={{ fontSize: '1.1rem', color: 'var(--text-main)', margin: 0 }}>User Reviews</h4>
                {selectedDetailItem.reviews_count > 0 && (
                  <span style={{ background: 'rgba(0, 200, 83, 0.1)', color: '#00C853', padding: '2px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 'bold' }}>
                    {selectedDetailItem.average_rating} ★ ({selectedDetailItem.reviews_count} {selectedDetailItem.reviews_count === 1 ? 'review' : 'reviews'})
                  </span>
                )}
              </div>

              {selectedDetailItem.reviews && selectedDetailItem.reviews.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', maxHeight: '200px', overflowY: 'auto', paddingRight: '0.5rem' }}>
                  {selectedDetailItem.reviews.map(review => (
                    <div key={review.id} style={{ background: 'rgba(255,255,255,0.03)', padding: '1rem', borderRadius: '12px', border: '1px solid var(--glass-border)' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          <div style={{ width: '24px', height: '24px', borderRadius: '50%', background: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.7rem', color: 'white', fontWeight: 'bold' }}>
                            {review.reviewer_name?.charAt(0).toUpperCase() || 'A'}
                          </div>
                          <span style={{ fontSize: '0.9rem', fontWeight: 'bold', color: 'var(--text-main)' }}>{review.reviewer_name || 'Anonymous'}</span>
                        </div>
                        <div style={{ display: 'flex', gap: '0.1rem', color: '#FFB400', fontSize: '0.8rem' }}>
                          {Array.from({ length: 5 }).map((_, i) => (
                            <span key={i} style={{ color: i < review.rating ? '#FFB400' : 'rgba(255,255,255,0.1)' }}>★</span>
                          ))}
                        </div>
                      </div>
                      <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>"{review.comment}"</p>
                      <span style={{ fontSize: '0.75rem', color: 'rgba(255,255,255,0.3)', display: 'block', marginTop: '0.5rem', textAlign: 'right' }}>
                        {review.created_at?.split('T')[0]}
                      </span>
                    </div>
                  ))}
                </div>
              ) : (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontStyle: 'italic', margin: 0 }}>No reviews yet for this item.</p>
              )}
            </div>

            {/* Actions */}
            <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
              <button 
                onClick={() => setSelectedDetailItem(null)} 
                className="btn" 
                style={{ flex: 1, background: 'rgba(255,255,255,0.1)', color: 'var(--text-main)' }}
              >
                Close
              </button>
              {user?.id !== selectedDetailItem.owner_id && selectedDetailItem.is_available && (
                <button 
                  onClick={() => {
                    const itemToReq = selectedDetailItem;
                    setSelectedDetailItem(null);
                    handleRequest(itemToReq);
                  }} 
                  className="btn btn-primary" 
                  style={{ flex: 1 }}
                >
                  Request to Borrow
                </button>
              )}
            </div>

          </div>
        </div>
      )}
    </div>
  );
}

export default Home;
