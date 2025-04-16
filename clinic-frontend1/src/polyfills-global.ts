// Polyfill for libraries expecting the Node.js 'global' object in browser environments
if (typeof (window as any).global === 'undefined') {
  (window as any).global = window;
}