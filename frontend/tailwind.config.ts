import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // Cool charcoal-navy: serious, "vault"-grade dark surfaces.
        ink: {
          950: '#080b11',
          900: '#0d131c',
          800: '#141c28',
          700: '#1f2a39',
          600: '#33415380'
        },
        // Single accent — desaturated emerald = money / growth / "go".
        brand: {
          50: '#e9f7f1',
          100: '#cdeede',
          300: '#7fd3b8',
          400: '#3cbd97',
          500: '#159b76',
          600: '#0f7e60',
          700: '#0c6650'
        },
        // Semantic only: expenses, destructive actions.
        coral: {
          50: '#fdecec',
          500: '#e2575f',
          600: '#cf4750'
        }
      },
      fontFamily: {
        display: ['Space Grotesk', 'sans-serif'],
        body: ['Manrope', 'sans-serif']
      },
      boxShadow: {
        // Tinted, soft — single light source, hue-matched to the ink ground.
        card: '0 1px 2px rgba(13,19,28,0.04), 0 14px 34px -16px rgba(13,19,28,0.18)',
        lift: '0 1px 2px rgba(13,19,28,0.05), 0 24px 48px -20px rgba(13,19,28,0.30)',
        glow: '0 16px 36px -16px rgba(21,155,118,0.55)'
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
        'fade-up': 'fadeUp 0.5s cubic-bezier(0.16, 1, 0.3, 1)',
        'float-slow': 'floatSlow 6s ease-in-out infinite'
      }
    }
  },
  plugins: []
} satisfies Config;
