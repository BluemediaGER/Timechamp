<template>
  <div class="bg-light min-vh-100 d-flex flex-row align-items-center">
    <CContainer>
      <CRow class="justify-content-center">
        <CCol :md="4">
          <CCard class="p-4">
            <CCardBody>
              <CForm>
                <h1>Login</h1>
                <p class="text-medium-emphasis">{{ $t("login.helpText") }}</p>
                <CInputGroup class="mb-3">
                  <CInputGroupText>
                    <CIcon icon="cil-user" />
                  </CInputGroupText>
                  <CFormInput
                    :placeholder="$t('login.usernamePlaceholder')"
                    autocomplete="username"
                  />
                </CInputGroup>
                <CInputGroup class="mb-4">
                  <CInputGroupText>
                    <CIcon icon="cil-lock-locked" />
                  </CInputGroupText>
                  <CFormInput
                    type="password"
                    :placeholder="$t('login.passwordPlaceholder')"
                    autocomplete="current-password"
                  />
                </CInputGroup>
                <CRow :xs="1">
                  <CCol>
                    <CButton color="primary" class="px-4">
                      {{ $t("login.submitButton") }}
                    </CButton>
                  </CCol>
                </CRow>
              </CForm>
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>
    </CContainer>
  </div>
</template>

<script>
export default {
  name: "Login",
};
</script>
