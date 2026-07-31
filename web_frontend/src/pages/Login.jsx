import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Login({ setUser }) {
  const [mode, setMode] = useState('login'); // login, register, verify-otp, forgot-password, reset-password
  const [formData, setFormData] = useState({ name: '', regNumber: '', email: '', password: '', otp: '', newPassword: '' });
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const validate = () => {
    if (mode === 'register') {
        if (!formData.regNumber.startsWith('19') || formData.regNumber.length !== 9) {
          return "Registration Number must start with 19 and be 9 digits long.";
        }
        if (!formData.name) return "Name is required.";
    }
    if (['login', 'register', 'forgot-password'].includes(mode)) {
        if (!formData.email.endsWith('@gmail.com')) {
          return "Email must end with @gmail.com";
        }
    }
    if (['login', 'register'].includes(mode) && !formData.password) {
        return "Password is required.";
    }
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');
    
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    try {
      let endpoint = '';
      let bodyData = {};
      
      if (mode === 'login') {
          endpoint = '/auth/login/';
          bodyData = { email: formData.email, password: formData.password };
      } else if (mode === 'register') {
          endpoint = '/auth/register/';
          bodyData = { name: formData.name, regNumber: formData.regNumber, email: formData.email, password: formData.password };
      } else if (mode === 'verify-otp') {
          endpoint = '/auth/verify-otp/';
          bodyData = { email: formData.email, otp: formData.otp };
      } else if (mode === 'forgot-password') {
          endpoint = '/auth/forgot-password/';
          bodyData = { email: formData.email };
      } else if (mode === 'reset-password') {
          endpoint = '/auth/reset-password/';
          bodyData = { email: formData.email, otp: formData.otp, newPassword: formData.newPassword };
      }

      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(bodyData)
      });
      
      const data = await response.json();
      
      if (response.ok) {
        if (mode === 'login') {
            localStorage.setItem('user', JSON.stringify(data));
            setUser(data);
            navigate('/');
        } else if (mode === 'register') {
            setMode('verify-otp');
            setSuccessMsg('OTP sent to your email.');
        } else if (mode === 'verify-otp') {
            localStorage.setItem('user', JSON.stringify(data));
            setUser(data);
            navigate('/');
        } else if (mode === 'forgot-password') {
            setMode('reset-password');
            setSuccessMsg('OTP sent to your email to reset password.');
        } else if (mode === 'reset-password') {
            setMode('login');
            setSuccessMsg('Password reset successfully. Please login.');
        }
      } else {
        setError(data.error || 'Operation failed');
        if (data.needs_verification) {
            setMode('verify-otp');
            // Normally would trigger another OTP send here or allow them to just enter
            setSuccessMsg('Please verify your email.');
        }
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
          src={`${import.meta.env.BASE_URL}login_illustration.png`} 
          alt="Students swapping items" 
          style={{ width: '100%', height: '240px', objectFit: 'contain', marginBottom: '2rem' }} 
        />

        <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'black', marginBottom: '0.5rem', textAlign: 'center' }}>
          {mode === 'login' && "Welcome to BorrowBuddy"}
          {mode === 'register' && "Create an Account"}
          {mode === 'verify-otp' && "Verify Email"}
          {mode === 'forgot-password' && "Forgot Password"}
          {mode === 'reset-password' && "Reset Password"}
        </h1>
        <p style={{ color: 'gray', fontSize: '0.9rem', textAlign: 'center', marginBottom: '2rem' }}>
          {mode === 'login' && "Enter your credentials to continue."}
          {mode === 'register' && "Join the community today."}
          {mode === 'verify-otp' && "Enter the OTP sent to your email."}
          {mode === 'forgot-password' && "Enter your email to receive an OTP."}
          {mode === 'reset-password' && "Enter OTP and your new password."}
        </p>
        
        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: 'var(--danger)', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', fontSize: '0.9rem', textAlign: 'center' }}>
            {error}
          </div>
        )}

        {successMsg && (
          <div style={{ background: 'rgba(76, 175, 80, 0.1)', color: '#4CAF50', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', fontSize: '0.9rem', textAlign: 'center' }}>
            {successMsg}
          </div>
        )}
        
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          
          {(mode === 'login' || mode === 'register' || mode === 'forgot-password') && (
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', color: 'gray', fontSize: '0.9rem' }}>Email Address</label>
              <input 
                type="email" 
                name="email" 
                value={formData.email} 
                onChange={handleChange} 
                required 
                autoComplete="off"
                style={{ width: '100%', padding: '1rem', borderRadius: '12px', border: '1px solid gray', background: 'transparent', outline: 'none' }}
                onFocus={(e) => e.target.style.borderColor = '#6C5CE7'}
                onBlur={(e) => e.target.style.borderColor = 'gray'}
              />
            </div>
          )}

          {mode === 'register' && (
            <>
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
            </>
          )}

          {(mode === 'login' || mode === 'register') && (
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', color: 'gray', fontSize: '0.9rem' }}>Password</label>
              <input 
                type="password" 
                name="password" 
                value={formData.password} 
                onChange={handleChange} 
                required 
                autoComplete="new-password"
                style={{ width: '100%', padding: '1rem', borderRadius: '12px', border: '1px solid gray', background: 'transparent', outline: 'none' }}
                onFocus={(e) => e.target.style.borderColor = '#6C5CE7'}
                onBlur={(e) => e.target.style.borderColor = 'gray'}
              />
            </div>
          )}

          {(mode === 'verify-otp' || mode === 'reset-password') && (
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', color: 'gray', fontSize: '0.9rem' }}>OTP</label>
              <input 
                type="text" 
                name="otp" 
                value={formData.otp} 
                onChange={handleChange} 
                required 
                style={{ width: '100%', padding: '1rem', borderRadius: '12px', border: '1px solid gray', background: 'transparent', outline: 'none', letterSpacing: '5px', textAlign: 'center', fontSize: '1.2rem', fontWeight: 'bold' }}
                onFocus={(e) => e.target.style.borderColor = '#6C5CE7'}
                onBlur={(e) => e.target.style.borderColor = 'gray'}
              />
            </div>
          )}

          {mode === 'reset-password' && (
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', color: 'gray', fontSize: '0.9rem' }}>New Password</label>
              <input 
                type="password" 
                name="newPassword" 
                value={formData.newPassword} 
                onChange={handleChange} 
                required 
                style={{ width: '100%', padding: '1rem', borderRadius: '12px', border: '1px solid gray', background: 'transparent', outline: 'none' }}
                onFocus={(e) => e.target.style.borderColor = '#6C5CE7'}
                onBlur={(e) => e.target.style.borderColor = 'gray'}
              />
            </div>
          )}

          {mode === 'login' && (
            <div style={{ textAlign: 'right', marginTop: '-0.5rem' }}>
               <span style={{ color: '#6C5CE7', fontSize: '0.9rem', cursor: 'pointer' }} onClick={() => { setMode('forgot-password'); setError(''); setSuccessMsg(''); }}>Forgot Password?</span>
            </div>
          )}

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
            {loading ? <span className="loader" style={{ width: '20px', height: '20px', borderWidth: '2px' }}></span> : 
              mode === 'login' ? 'Login' : 
              mode === 'register' ? 'Register' :
              mode === 'verify-otp' ? 'Verify' :
              mode === 'forgot-password' ? 'Send OTP' : 'Reset Password'
            }
          </button>
        </form>

        <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
           {mode === 'login' && <span style={{ color: 'gray', fontSize: '0.9rem' }}>Don't have an account? <span style={{ color: '#6C5CE7', cursor: 'pointer', fontWeight: 'bold' }} onClick={() => { setMode('register'); setError(''); setSuccessMsg(''); }}>Register</span></span>}
           {(mode === 'register' || mode === 'forgot-password' || mode === 'reset-password') && <span style={{ color: 'gray', fontSize: '0.9rem' }}>Back to <span style={{ color: '#6C5CE7', cursor: 'pointer', fontWeight: 'bold' }} onClick={() => { setMode('login'); setError(''); setSuccessMsg(''); }}>Login</span></span>}
        </div>
      </div>
    </div>
  );
}

export default Login;
