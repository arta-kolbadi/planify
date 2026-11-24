---
name: PLANIFY
colors:
  surface: '#191724'
  surface-dim: '#13121a'
  surface-bright: '#3a3841'
  surface-container-lowest: '#0e0d15'
  surface-container-low: '#1c1b23'
  surface-container: '#201f27'
  surface-container-high: '#2a2932'
  surface-container-highest: '#35343d'
  on-surface: '#e5e0ec'
  on-surface-variant: '#cac3d6'
  inverse-surface: '#e5e0ec'
  inverse-on-surface: '#312f38'
  outline: '#948e9f'
  outline-variant: '#494553'
  surface-tint: '#cdbdff'
  primary: '#cdbdff'
  on-primary: '#370095'
  primary-container: '#9a7aff'
  on-primary-container: '#300084'
  inverse-primary: '#6744cb'
  secondary: '#ccbdff'
  on-secondary: '#341f74'
  secondary-container: '#4e3a8f'
  on-secondary-container: '#bfadff'
  tertiary: '#ccbff6'
  on-tertiary: '#332957'
  tertiary-container: '#9589be'
  on-tertiary-container: '#2c224f'
  error: '#F07885'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e8deff'
  primary-fixed-dim: '#cdbdff'
  on-primary-fixed: '#20005f'
  on-primary-fixed-variant: '#4f26b2'
  secondary-fixed: '#e7deff'
  secondary-fixed-dim: '#ccbdff'
  on-secondary-fixed: '#1f005f'
  on-secondary-fixed-variant: '#4b388c'
  tertiary-fixed: '#e7deff'
  tertiary-fixed-dim: '#ccbff6'
  on-tertiary-fixed: '#1e1340'
  on-tertiary-fixed-variant: '#4a406f'
  background: '#13121a'
  on-background: '#e5e0ec'
  surface-variant: '#35343d'
  surface-elevated: '#211E30'
  text-primary: '#F7F5FF'
  text-secondary: '#AAA5BA'
  border: '#302B40'
  success: '#54C69A'
  warning: '#F4BC69'
  brand-blue: '#0095FF'
typography:
  display:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 45px
  display-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 35px
  h1:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
  h2:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  h3:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  h4:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 25px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
  body:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
  small:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
  caption:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '400'
    lineHeight: 15px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes the visual and interactive foundation for **PLANIFY** (*Your Life. Your Focus. Your Progress.*), a multi-platform productivity, study planning, AI assistance, and digital wellbeing ecosystem spanning Web, Windows, and Android.

### Brand Personality & Audience
- **Personality:** Intelligent, friendly, modern, calm, encouraging, organized, playful, and trustworthy.
- **Target Audience:** Students, lifelong learners, and productivity-focused individuals seeking structured daily planning without overwhelming complexity.
- **Emotional Response:** Focus without pressure, clarity through organization, and gentle motivationâ€”supported by a customizable 2D virtual companion and celebratory micro-interactions.

### Design Style
The aesthetic blends **Corporate / Modern** clarity with **selective Glassmorphism** and **Tactile** warmth:
- **Structured Productivity:** Clean, high-legibility layouts, clear visual hierarchy, and predictable navigation patterns.
- **Glossy Brand Identity:** Anchored by the glossy blue orbital "P" brand mark and sparkle accents, used sparingly for brand moments, AI highlights, and achievement celebrations.
- **Companion Warmth:** Rounded, approachable surfaces and expressive 2D character states that soften the rigor of timers, task lists, and analytics.

## Colors

The default color mode is **dark**, using the **Midnight Purple** palette as the primary theme. All colors are mapped to semantic tokens so that theme switching (across Midnight Purple, Cozy Green, Ocean Blue, Sakura Pink, Minimal Light, AMOLED Black, and Sunset Orange) updates the entire interface consistently.

### Midnight Purple Token Roles
- **Background (`#111018`):** Primary application canvas.
- **Surface (`#191724`):** Standard cards, sidebars, and content containers.
- **Elevated Surface (`#211E30`):** Modals, dropdowns, popovers, and active hover states.
- **Primary (`#9877FF`):** Primary actions, active navigation indicators, focus rings, and key progress bars.
- **Secondary (`#B7A3FF`):** Secondary buttons, subtle highlights, and supporting data visualizations.
- **Accent (`#D4C7FF`):** Badges, active tags, and delicate decorative accents.
- **Text Primary (`#F7F5FF`) & Text Secondary (`#AAA5BA`):** High-contrast body copy and muted metadata, maintaining WCAG-oriented legibility against dark surfaces.
- **Border (`#302B40`):** Subtle structural dividers and input outlines.
- **Semantic Status:** `success` (`#54C69A`), `warning` (`#F4BC69`), and `error` (`#F07885`) for task states, Focus Lock alerts, and form validation.
- **Brand Blue (`#0095FF`):** Derived from the glossy blue Planify brand identity; reserved for the brand logo, Planify AI sparkle accents, and special milestone highlights.

## Typography

The type system is designed for high-density productivity screens, long-form study notes, and bilingual **RTL/LTR** environments.

### Font Families & Localization
- **English (LTR):** `Inter`, falling back to `system-ui, sans-serif`.
- **Persian (RTL):** `Vazirmatn`, falling back to `system-ui, sans-serif`.
- **Mixed-Language & Numerals:** Support seamless switching between Latin and Persian numerals, as well as Gregorian and Jalali calendar formatting.

### Hierarchy & Line Height
- **Headings (`display` through `h4`):** Tighter line heights (`1.25`â€“`1.4`) to anchor dashboards, planner headers, and modal titles. On mobile viewports, `display` scales down to `display-mobile` (`28px`) to preserve screen real estate.
- **Body (`body-lg`, `body`):** Generous `1.6` line height for comfortable reading in Notes, Academy lessons, and Planify AI responses.
- **Compact Controls (`small`, `caption`):** `1.4` line height for badges, timestamps, and metadata. Never use `caption` (`11px`) for essential interactive labels.

## Layout & Spacing

The layout uses a **12-column fluid grid** on desktop, adapting to an **8-column grid** on tablet and a **4-column single-flow grid** on mobile, all built on a strict **4px (`0.25rem`) spacing scale**.

### Responsive Breakpoints & Page Padding
- **Large Desktop (`1440px+`) & Desktop (`1024â€“1439px`):** Persistent left/start sidebar, multi-column dashboard widgets, expanded analytics, and optional contextual right/end panel. Page padding: `28â€“32px`.
- **Tablet (`768â€“1023px`):** Collapsible sidebar, adaptive 2-column grids, and touch-friendly controls. Page padding: `20â€“24px`.
- **Mobile (`360â€“767px`) & Small Mobile (`320â€“359px`):** Bottom navigation bar (Home, Planner, Focus, AI, Profile), single-column layouts, full-screen Focus Timer, and native-style dialogs. Page padding: `16px`.

### RTL/LTR Logical Layout
All layout spacing, margins, and directional alignments must use **CSS logical properties** (`margin-inline`, `padding-inline`, `inset-inline-start`, `inset-inline-end`) rather than hardcoded `left`/`right` values so that Persian RTL and English LTR mirror cleanly.

## Elevation & Depth

In the Midnight Purple dark theme, visual hierarchy is communicated primarily through **tonal surface layering** rather than heavy drop shadows:

- **Surface Contrast:** Stacked tiers progress from Background (`#111018`) to Surface (`#191724`) and Elevated Surface (`#211E30`), paired with subtle `1px` borders (`#302B40`) to define card boundaries cleanly.
- **Subtle Ambient Shadows:**
  - **Cards:** Minimal, low-opacity ambient shadow to gently lift content off the canvas.
  - **Dropdowns & Popovers:** Medium elevation with a tighter shadow spread to separate floating menus from underlying lists.
  - **Modals & Dialogs:** Stronger elevation paired with a dimmed backdrop scrim to focus user attention.
- **Selective Glassmorphism:** Translucent surfaces with `backdrop-filter: blur(12px)` are permitted for premium floating elementsâ€”such as the active Focus Timer HUD, Planify AI floating composer, and Study Room ambient controlp %›İšYY^ÛÛ˜\İ™[XZ[œÈİšXİHYÚX›K‚‚ˆÈÈÚ\\Â‚•HÚ\H[™İXYÙH\È
Š”›İ[™Y
Šˆ
›İ[™Y™\ÜÎˆ˜
K˜[[˜Ú[™È[Ù\›ˆÛÙØ\™H™XÚ\Ú[ÛˆÚ]H\›ØXÚX›KœšY[™H\œÛÛ˜[]HÙˆH‘š\X[ÛÛ\[š[Û‹‚‚ˆÈÈÈÛÛ\Û™[˜Y]\ÈØØ[B‹H
Š”ÛX[]ÛœÎŠŠˆ
\™[X
B‹H
Š’[œ]È	ˆ›Ü›HÛÛ›ÛÎŠŠˆL
Œ\™[X
B‹H
Š”İ[™\™Ø\™ÎŠŠˆMœ
\™[X
B‹H
Š“\™ÙHØ\™È	ˆ[Ù[ÎŠŠˆŒ
KŒ\™[X
B‹H
Š‘X[ÙÜÎŠŠˆ
K\™[X
B‹H
Š”[ËÚ\È	ˆ˜YÙ\ÎŠŠˆNN\
[Ø\İ[JB‚ˆÈÈÛÛ\Û™[Â‚[ÛÛ\Û™[È]\İİ\Ü
Š‘Y˜][İ™\‹›Øİ\ËXİ]™K\ØX›YØY[™ËİXØÙ\ÜË\œ›Ü‹[™[\JŠˆİ]\ËÚ]Z[š[][H0åÍİXÚ\™Ù]ÈÛˆ[Øš[H[™ÛX\ˆÙ^X›Ø\™›Øİ\Èš[™ÜÈ
ÎNÍÑ‘˜
K‚‚‹H
Š]ÛœÎŠŠˆÚ^˜\šX[(	E&–Ö'’†3“ƒstdff–ÆÂ’Â6V6öæF'’†3#S3f–ÆÂv—F‚4#t4dfFW‡B’Â÷WFÆ–æR†33$#C&÷&FW"’Âv†÷7BÂFævW"†4csƒƒV’ÂæB–6öâöæÇ’âÖ–7&òÖ–çFW&7F–öç2æ–ÖFR–â#(	3ƒ×6à¢Ò¢¤6†—2b&FvW3¢¢¢–ÆÂ×6†VB†““—†’6öçF–æW'2W6–ær3#S3÷"F–çFVB6VÖçF–2&6¶w&÷VæG2f÷"F6²7V&¦V7G2Â&–÷&—F–W2ÂæB’76—7FçBFw2à¢Ò¢¤Æ—7G3¢¢¢6ÆVâfW'F–6Â7F6·2v—F‚76R×6Ö†‡†’Fò76RÖÖF†g†’v2Â7W÷'F–ær%DÂÖv&RÆVF–ær–6öç2„ÇV6–FR–6öç2Â6öç6—7FVçB7G&ö¶Rv–GF‚’æBG&–Æ–ær7F–öç2à¢Ò¢¤6†V6¶&÷†W2b&F–ò'WGFöç3¢¢¢†÷"6—&7VÆ"6öçG&öÇ2v—F‚33$#C&÷&FW'2ÂG&ç6—F–öæ–ær6Öö÷F†Ç’Fò3“ƒstdfv†Vâ6VÆV7FVBà¢Ò¢¤–çWBf–VÆG3¢¢¢†&F—W2Â3“s#F÷"3†&6¶w&÷VæBÂ33$#C&÷&FW"ÂæB3“ƒstdffö7W2&–ærâ–æ6ÇVFW2FW‡BÂ77v÷&BÂ6V&6‚ÂFW‡F&VÂFFRõF–ÖR–6¶W'2„¦ÆÆ’bw&Vv÷&–â’Â6VÆV7BÂFövvÆRÂæB6Æ–FW"à¢Ò¢¤6&G3¢¢¢g†(	6#†&F—W2öâ3“s#F7W&f6Rv—F‚†33$#C&÷&FW"æB76RÖÖF†g†’Fò76RÖÆv†#G†’–çFW&æÂFF–ærâ6÷fW'2F6†&ö&BÂF6²Âfö7W2Â’ÖW76vRÂ6†&7FW"Â7FF—7F–72Â6†–WfVÖVçBÂæB7V&¦V7B6&G2à¢Ò¢¥&öGV7BÕ7V6–f–2ÖöGVÆW3¢¢ ¢Ò¢¤fö7W2F–ÖW#¢¢¢f—7VÆÇ’&öÖ–æVçB6—&7VÆ"÷"çVÖW&–6Â…TBv—F‚6ÆV"7FFR–æF–6F÷'2„–FÆRÂ'Vææ–ærÂW6VBÂ'&V²Â6ö×ÆWFVB’à¢Ò¢¥Ææ–g’“¢¢¢Ö&¶F÷vâÖVæ&ÆVBÖW76vR6&G2Â6öFR&Æö6·2Â76—7FçBöÖöFVÂ6VÆV7F÷'2ÂæB7VvvW7FVB&ö×B6†—2à¢Ò¢¤æf–vF–öã¢¢¢W'6—7FVçBFW6·F÷6–FV&"„F6†&ö&BÂÆææW"Â6ÆVæF"ÂF6·2Âfö7W2F–ÖW"Âfö7W2Æö6²ÂÆæ–g’’Â6FV×’Â×’6†&7FW"Â7GVG’&ööÒÂæ÷FW2Â7FF—7F–72Â6†–WfVÖVçG2Â6WGF–æw2’æBÖö&–ÆR&÷GFöÒæf–vF–öâ„†öÖRÂÆææW"Âfö7W2Â’Â&öf–ÆR’