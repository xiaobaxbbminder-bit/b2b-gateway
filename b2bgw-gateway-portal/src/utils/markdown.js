import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: false,
  typographer: false,
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang, ignoreIllegals: true }).value
      } catch (_) {}
    }
    try {
      return hljs.highlightAuto(code).value
    } catch (_) {
      return md.utils.escapeHtml(code)
    }
  }
})

const defaultLinkOpen = md.renderer.rules.link_open ||
  function (tokens, idx, options, env, self) {
    return self.renderToken(tokens, idx, options)
  }

md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  const token = tokens[idx]
  const targetIdx = token.attrIndex('target')
  if (targetIdx < 0) {
    token.attrPush(['target', '_blank'])
  } else {
    token.attrs[targetIdx][1] = '_blank'
  }
  const relIdx = token.attrIndex('rel')
  if (relIdx < 0) {
    token.attrPush(['rel', 'noopener noreferrer'])
  } else {
    token.attrs[relIdx][1] = 'noopener noreferrer'
  }
  return defaultLinkOpen(tokens, idx, options, env, self)
}

export function renderMarkdown(text) {
  if (!text) return ''
  return md.render(text)
}
