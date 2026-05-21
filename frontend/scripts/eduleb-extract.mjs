/**
 * Extracts main markup from static Eduleb HTML into Angular template fragments.
 * Run: node scripts/eduleb-extract.mjs
 */
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, '..');
const eduleb = path.join(root, 'eduleb');
const pagesDir = path.join(root, 'src', 'app', 'eduleb', 'pages');

const hrefMap = new Map([
  ['index.html', '/home'],
  ['index2.html', '/home-2'],
  ['about.html', '/about'],
  ['contact.html', '/contact'],
  ['course.html', '/course'],
  ['course_details.html', '/course-details'],
  ['instructor.html', '/instructors'],
  ['ins_details.html', '/instructor-details'],
  ['pricing.html', '/pricing'],
  ['faq.html', '/faq'],
  ['404.html', '/not-found'],
  ['blog.html', '/blog'],
  ['blog_single.html', '/blog-single'],
  ['single_blog.html', '/blog-single'],
  ['thank-you.html', '/thank-you'],
]);

function rewriteMarkup(html) {
  let out = html.replace(/assets\//g, '/eduleb/assets/');
  for (const [file, route] of hrefMap) {
    out = out.replaceAll(`href="${file}"`, `href="${route}"`);
  }
  // Angular template parser treats @ as syntax; escape email addresses
  out = out.replace(/([a-zA-Z0-9._-])@([a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/g, '$1&#64;$2');
  return out;
}

function extractBetween(src, start, end) {
  const i = src.indexOf(start);
  const j = src.indexOf(end);
  if (i === -1 || j === -1 || j <= i) {
    throw new Error(`Markers not found for ${start} / ${end}`);
  }
  return src.slice(i, j);
}

const pageDefs = [
  { file: 'index.html', folder: 'home', start: '<!-- START HOME -->', end: '<!-- START FOOTER -->' },
  { file: 'index2.html', folder: 'home-2', start: '<!-- START HOME -->', end: '<!-- START FOOTER -->' },
  { file: 'about.html', folder: 'about', start: '<!-- START SECTION TOP -->', end: '<!-- START FOOTER -->' },
  { file: 'contact.html', folder: 'contact', start: '<!-- START SECTION TOP -->', end: '<!-- START FOOTER -->' },
  { file: 'course.html', folder: 'course', start: '<!-- START SECTION TOP -->', end: '<!-- START FOOTER -->' },
  { file: 'course_details.html', folder: 'course-details', start: '<!-- START SECTION TOP -->', end: '<!-- START FOOTER -->' },
  { file: 'instructor.html', folder: 'instructors', start: '<!-- START SECTION TOP -->', end: '<!-- START FOOTER -->' },
  { file: 'ins_details.html', folder: 'instructor-details', start: '<!-- START SECTION TOP -->', end: '<!-- START FOOTER -->' },
  { file: 'pricing.html', folder: 'pricing', start: '<!-- START SECTION TOP -->', end: '<!-- START FOOTER -->' },
  { file: 'faq.html', folder: 'faq', start: '<!-- START SECTION TOP -->', end: '<!-- START FOOTER -->' },
  { file: 'blog.html', folder: 'blog', start: '<!-- START SECTION TOP -->', end: '<!-- START FOOTER -->' },
  { file: 'blog_single.html', folder: 'blog-single', start: '<!-- START SECTION TOP -->', end: '<!-- START FOOTER -->' },
  { file: '404.html', folder: 'not-found', start: '<!-- START SECTION TOP -->', end: '<!-- START FOOTER -->' },
];

const nameByFolder = {
  home: 'Home',
  'home-2': 'Home2',
  about: 'About',
  contact: 'Contact',
  course: 'Course',
  'course-details': 'CourseDetails',
  instructors: 'Instructors',
  'instructor-details': 'InstructorDetails',
  pricing: 'Pricing',
  faq: 'Faq',
  blog: 'Blog',
  'blog-single': 'BlogSingle',
  'not-found': 'NotFound',
};

for (const def of pageDefs) {
  const raw = fs.readFileSync(path.join(eduleb, def.file), 'utf8');
  let body = extractBetween(raw, def.start, def.end);
  body = rewriteMarkup(body);
  const dir = path.join(pagesDir, def.folder);
  fs.mkdirSync(dir, { recursive: true });
  const fileBase = def.folder;
  const cls = nameByFolder[def.folder] + 'PageComponent';
  fs.writeFileSync(path.join(dir, `${fileBase}.component.html`), body.trim() + '\n');
  fs.writeFileSync(
    path.join(dir, `${fileBase}.component.ts`),
    `import { Component } from '@angular/core';

@Component({
  selector: 'app-eduleb-${def.folder.replace(/[^a-z0-9-]/g, '')}',
  standalone: true,
  templateUrl: './${fileBase}.component.html',
})
export class ${cls} {}
`,
  );
}

const thankRaw = fs.readFileSync(path.join(eduleb, 'thank-you.html'), 'utf8');
const thankMatch = thankRaw.match(/<section[\s\S]*?<\/section>/i);
const thankBody = thankMatch ? rewriteMarkup(thankMatch[0]) : '<p>Thank you</p>';
const thankDir = path.join(pagesDir, 'thank-you');
fs.mkdirSync(thankDir, { recursive: true });
fs.writeFileSync(path.join(thankDir, 'thank-you.component.html'), thankBody.trim() + '\n');
fs.writeFileSync(
  path.join(thankDir, 'thank-you.component.ts'),
  `import { Component } from '@angular/core';

@Component({
  selector: 'app-eduleb-thank-you',
  standalone: true,
  templateUrl: './thank-you.component.html',
})
export class ThankYouPageComponent {}
`,
);

console.log('Eduleb templates extracted to src/app/eduleb/pages/');
