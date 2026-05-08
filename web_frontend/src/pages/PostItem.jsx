import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function PostItem({ user }) {
  const [categories, setCategories] = useState([]);
  const [formData, setFormData] = useState({ title: '', description: '', condition: 'Good', category: '', max_borrow_days: 7 });
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/categories/`);
      const data = await response.json();
      setCategories(data);
      if (data.length > 0) setFormData(prev => ({ ...prev, category: data[0].id }));
    } catch (err) {
      console.error('Failed to fetch categories');
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleImageChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setImage(e.target.files[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const data = new FormData();
    data.append('owner', user.id);
    data.append('title', formData.title);
    data.append('description', formData.description);
    data.append('condition', formData.condition);
    data.append('category', formData.category);
    data.append('max_borrow_days', formData.max_borrow_days);
    data.append('is_available', 'true');
    if (image) {
      data.append('image', image);
    }

    try {
      const response = await fetch(`${API_BASE_URL}/items/`, {
        method: 'POST',
        body: data, // No Content-Type header so browser sets multipart boundary
      });

      if (response.ok) {
        navigate('/');
      } else {
        const errorData = await response.json();
        setError(JSON.stringify(errorData));
      }
    } catch (err) {
      setError('Network error. Failed to post item.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fade-in" style={{ maxWidth: '600px', margin: '0 auto' }}>
      <h2 style={{ fontSize: '2rem', marginBottom: '1.5rem' }}>Post an Item to Lend</h2>
      
      <div className="glass-card" style={{ padding: '2rem' }}>
        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger)', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Item Title</label>
            <input type="text" name="title" value={formData.title} onChange={handleChange} required placeholder="e.g. Scientific Calculator" />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Category</label>
            <select 
              name="category" 
              value={formData.category} 
              onChange={handleChange} 
              required
            >
              {categories.map(cat => (
                <option key={cat.id} value={cat.id} style={{ color: 'black' }}>{cat.name}</option>
              ))}
            </select>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Description</label>
            <textarea 
              name="description" 
              value={formData.description} 
              onChange={handleChange} 
              required 
              placeholder="Provide details about the item..."
              style={{ minHeight: '120px' }}
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Condition</label>
            <select name="condition" value={formData.condition} onChange={handleChange} required>
              <option value="New" style={{ color: 'black' }}>New</option>
              <option value="Like New" style={{ color: 'black' }}>Like New</option>
              <option value="Good" style={{ color: 'black' }}>Good</option>
              <option value="Fair" style={{ color: 'black' }}>Fair</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Max Borrow Duration (Days)</label>
            <input type="number" name="max_borrow_days" min="1" max="90" value={formData.max_borrow_days} onChange={handleChange} required />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Item Image (Optional)</label>
            <input type="file" accept="image/*" onChange={handleImageChange} style={{ padding: '0.5rem' }} />
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading} style={{ marginTop: '1rem', padding: '1rem' }}>
            {loading ? <span className="loader"></span> : 'Post Item'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default PostItem;
