import { createI18n } from 'vue-i18n'
import zhHans from './modules/zh-Hans.js'
import zhHant from './modules/zh-Hant.js'
import enUs from './modules/en-US.js'

const vueI18n = createI18n({
  locale: 'en-US',
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
