import React from 'react';
import { useNavigate } from 'react-router-dom';

function ThankYou() {
  const navigate = useNavigate();

  return (
    <div className="fade-in" style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '70vh',
      maxWidth: '600px',
      margin: '0 auto',
      padding: '2rem',
      textAlign: 'center'
    }}>
      {/* Animated Heart Icon Container */}
      <div style={{
        position: 'relative',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: '2rem'
      }}>
        <div style={{
          width: '160px',
          height: '160px',
          borderRadius: '50%',
          background: 'rgba(108, 92, 231, 0.05)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          animation: 'pulse 2s infinite alternate'
        }}>
          <div style={{
            width: '120px',
            height: '120px',
            borderRadius: '50%',
            background: 'rgba(108, 92, 231, 0.1)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <span style={{ fontSize: '4rem', filter: 'drop-shadow(0 4px 6px rgba(108, 92, 231, 0.3))' }}>❤️</span>
          </div>
        </div>
      </div>

      <h1 style={{
        fontSize: '2.5rem',
        fontWeight: '800',
        marginBottom: '1rem',
        background: 'linear-gradient(135deg, var(--primary), var(--secondary))',
        WebkitBackgroundClip: 'text',
        WebkitTextFillColor: 'transparent',
        color: 'var(--primary)'
      }}>
        You're Awesome! 💜
      </h1>

      <p style={{
        fontSize: '1.1rem',
        color: 'var(--text-muted)',
        lineHeight: '1.6',
        marginBottom: '3rem'
      }}>
        You returned the item.<br />
        It's now available on the homepage for others to borrow again!
      </p>

      {/* Rewards Cards */}
      <div style={{
        display: 'flex',
        gap: '1.5rem',
        width: '100%',
        marginBottom: '3.5rem'
      }}>
        <div className="glass-card" style={{
          flex: 1,
          padding: '1.5rem',
          textAlign: 'center',
          borderLeft: '4px solid var(--primary)',
          background: 'var(--surface-color)',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center'
        }}>
          <div style={{
            fontSize: '2rem',
            fontWeight: '900',
            color: 'var(--primary)',
            marginBottom: '0.25rem'
          }}>+10</div>
          <div style={{
            fontSize: '0.85rem',
            fontWeight: '700',
            color: 'var(--text-muted)'
          }}>Trust Score</div>
        </div>

        <div className="glass-card" style={{
          flex: 1,
          padding: '1.5rem',
          textAlign: 'center',
          borderLeft: '4px solid var(--secondary)',
          background: 'var(--surface-color)',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center'
        }}>
          <div style={{
            fontSize: '2rem',
            fontWeight: '900',
            color: 'var(--secondary)',
            marginBottom: '0.25rem'
          }}>+20</div>
          <div style={{
            fontSize: '0.85rem',
            fontWeight: '700',
            color: 'var(--text-muted)'
          }}>Helper Points</div>
        </div>
      </div>

      {/* Action Buttons */}
      <button 
        className="btn btn-primary" 
        onClick={() => navigate('/')} 
        style={{
          width: '100%',
          height: '56px',
          fontSize: '1.1rem',
          fontWeight: 'bold',
          marginBottom: '1rem',
          background: 'linear-gradient(to right, #8E2DE2, #4A00E0)'
        }}
      >
        Keep Helping
      </button>

      <button 
        className="btn" 
        onClick={() => navigate('/')} 
        style={{
          background: 'transparent',
          color: 'var(--text-muted)',
          fontWeight: '600'
        }}
      >
        Back to Home
      </button>

      {/* Add CSS pulse animation block style locally */}
      <style>{`
        @keyframes pulse {
          0% { transform: scale(1); }
          100% { transform: scale(1.1); }
        }
      `}</style>
    </div>
  );
}

export default ThankYou;
