import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Login({ setUser }) {
  const [formData, setFormData] = useState({ name: '', regNumber: '', email: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const validate = () => {
    if (!formData.regNumber.startsWith('19') || formData.regNumber.length !== 9) {
      return "Registration Number must start with 19 and be 9 digits long.";
    }
    if (!formData.email.endsWith('.sse@saveetha.com')) {
      return "Email must end with .sse@saveetha.com";
    }
    if (!formData.name) return "Name is required.";
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login/`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
      
      const data = await response.json();
      
      if (response.ok) {
        localStorage.setItem('user', JSON.stringify(data));
        setUser(data);
        navigate('/');
      } else {
        setError(data.error || 'Login failed');
      }
    } catch (err) {
      setError('Network error. Is the backend running?');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '80vh', padding: '1.5rem', background: 'white' }} className="fade-in">
      <div style={{ width: '100%', maxWidth: '400px' }}>
        
        <img 
          src="/login_illustration.png" 
          alt="Students swapping items" 
          style={{ width: '100%', height: '240px', objectFit: 'contain', marginBottom: '2rem' }} 
        />

        <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'black', marginBottom: '0.5rem', textAlign: 'center' }}>
          Welcome to BorrowBuddy
        </h1>
        <p style={{ color: 'gray', fontSize: '0.9rem', textAlign: 'center', marginBottom: '2rem' }}>
          Enter your details to continue.
        </p>
        
        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger)', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', fontSize: '0.9rem', textAlign: 'center' }}>
            {error}
          </div>
        )}
        
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'gray', fontSize: '0.9rem' }}>Name</label>
            <input 
              type="text" 
              name="name" 
              value={formData.name} 
              onChange={handleChange} 
              required 
              style={{ width: '100%', padding: '1rem', borderRadius: '12px', border: '1px solid gray', background: 'transparent', outline: 'none' }}
              onFocus={(e) => e.target.style.borderColor = '#6C5CE7'}
              onBlur={(e) => e.target.style.borderColor = 'gray'}
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'gray', fontSize: '0.9rem' }}>College Registration Number</label>
            <input 
              type="text" 
              name="regNumber" 
              value={formData.regNumber} 
              onChange={(e) => {
                const val = e.target.value;
                if (val.length <= 9 && /^\d*$/.test(val)) {
                  handleChange(e);
                }
              }} 
              required 
              style={{ width: '100%', padding: '1rem', borderRadius: '12px', border: '1px solid gray', background: 'transparent', outline: 'none' }}
              onFocus={(e) => e.target.style.borderColor = '#6C5CE7'}
              onBlur={(e) => e.target.style.borderColor = 'gray'}
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'gray', fontSize: '0.9rem' }}>College Email Address</label>
            <input 
              type="email" 
              name="email" 
              value={formData.email} 
              onChange={handleChange} 
              required 
              style={{ width: '100%', padding: '1rem', borderRadius: '12px', border: '1px solid gray', background: 'transparent', outline: 'none' }}
              onFocus={(e) => e.target.style.borderColor = '#6C5CE7'}
              onBlur={(e) => e.target.style.borderColor = 'gray'}
            />
          </div>

          <button 
            type="submit" 
            disabled={loading} 
            style={{ 
              marginTop: '1.5rem', padding: '1.2rem', width: '100%',
              background: 'linear-gradient(to right, #6C5CE7, #A855F7)',
              color: 'white', border: 'none', borderRadius: '12px',
              fontSize: '1rem', fontWeight: 'bold', cursor: 'pointer'
            }}
          >
            {loading ? <span className="loader" style={{ width: '20px', height: '20px', borderWidth: '2px' }}></span> : 'Login'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;
