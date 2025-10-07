import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        // Palette principale
        primary: {
          DEFAULT: '#3B82F6',
          hover: '#2563EB',
        },
        accent: {
          DEFAULT: '#60A5FA',
          hover: '#3B82F6',
        },
        neutral: {
          dark: '#1F2937',
          light: '#F3F4F6',
        },
        // Couleurs de statut
        success: '#27AE60',
        error: '#E74C3C',
        warning: '#F39C12',
        info: '#3498DB',
        // Gris
        gray: {
          DEFAULT: '#95A5A6',
          50: '#F7F9FC',
          100: '#ECF0F1',
          200: '#D5DBDB',
          300: '#BDC3C7',
          400: '#95A5A6',
          500: '#7F8C8D',
          600: '#707B7C',
          700: '#5D6D7E',
          800: '#34495E',
          900: '#2C3E50',
        },
      },
      borderRadius: {
        DEFAULT: '8px',
      },
      transitionDuration: {
        DEFAULT: '200ms',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['Fira Code', 'monospace'],
      },
    },
  },
  plugins: [],
};
export default config;
