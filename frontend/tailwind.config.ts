import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: {
          900: '#0c1116',
          800: '#121923',
          700: '#1b2633'
        },
        slate: {
          200: '#e6edf5',
          300: '#d2dbe6',
          400: '#a8b4c4'
        },
        mint: {
          500: '#12b886',
          600: '#0fa77a'
        },
        gold: {
          500: '#f5b83b'
        },
        coral: {
          500: '#ef6f6c'
        }
      },
      fontFamily: {
        display: ['Space Grotesk', 'sans-serif'],
        body: ['Manrope', 'sans-serif']
      },
      boxShadow: {
        glow: '0 10px 30px rgba(18, 184, 134, 0.25)'
      },
      keyframes: {
        fadeUp: {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        },
        floatSlow: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-6px)' }
        }
      },
      animation: {
        'fade-up': 'fadeUp 0.6s ease-out',
        'float-slow': 'floatSlow 6s ease-in-out infinite'
      }
    }
  },
  plugins: []
} satisfies Config;
