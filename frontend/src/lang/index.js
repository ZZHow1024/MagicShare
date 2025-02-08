import { createI18n } from 'vue-i18n'
import zhHans from './modules/zh-Hans.js'
import zhHant from './modules/zh-Hant.js'
import enUs from './modules/en-US.js'

let language = navigator.language || navigator.userLanguage

if (language.includes('zh')) {
  if (language.includes('CN') || language.includes('cn')) language = 'zh-Hans'
  else if (language.includes('Hans') || language.includes('hans')) language = 'zh-Hans'
  else if (language.includes('Hant') || language.includes('hant')) language = 'zh-Hant'
  else language = 'zh-Hant'
} else {
  language = 'en-US'
}

const vueI18n = createI18n({
  locale: language,
  messages: {
    'zh-Hans': {
      ...zhHans,
    },
    'zh-Hant': {
      ...zhHant,
    },
    'en-US': {
      ...enUs,
    },
  },
})

export { vueI18n }
