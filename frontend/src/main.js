// Vue base
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

const app = createApp(App);
app.use(router);

// CoreUI
import CoreuiVue from "@coreui/vue";
import CIcon from "@coreui/icons-vue";
import { iconsSet as icons } from "@/assets/icons";
import "@coreui/coreui/dist/css/coreui.min.css";

app.use(CoreuiVue);
app.component("CIcon", CIcon);
app.provide("icons", icons);

// i18n - localisation
import { createI18n } from "vue-i18n";
import { langDe } from "@/assets/locals/de.js";
import { langEn } from "@/assets/locals/en.js";

const i18n = createI18n({
  locale: navigator.language.split("-")[0],
  fallbackLocale: "en",
  messages: {
    en: langEn,
    de: langDe,
  },
});
app.use(i18n);

// Axios
import { axios } from "axios";
axios.defaults.baseURL = "/api/v1";
axios.defaults.withCreditentials = true;

// Mount app
app.mount("#app");
