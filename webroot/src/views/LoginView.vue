<template>
    <div class="LoginView">
        <v-form>
          <v-container>
            <v-row>
                <v-col cols="12" sm="6">
                  <v-text-field label="Benutzername" v-model="loginData.username"></v-text-field>
                  <v-text-field password label="Passwort" v-model="loginData.password"></v-text-field>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="12">
                    <v-btn v-on:click="login">Anmelden</v-btn>
                </v-col>
            </v-row>
          </v-container>
        </v-form>
    </div>
</template>

<script>
// @ is an alias to /src


export default {
  name: 'LoginView',
  data:()=>({
    loginData: {
      username: '',
      password: '',
    }

  }),
  methods:{
        login: function(){
          const urlParams = new URLSearchParams(window.location.search);
          const redirect = urlParams.get('redirect');
          console.log(urlParams)
          console.log(redirect)


          console.log('login')
          console.log(this.loginData)
           fetch("http://localhost:8080/api/v1/postLogin", {
                "method": "POST",
                 "body": JSON.stringify(this.loginData)
            }).then(response =>{
                console.log('response222:')
                console.log(response)
                if(response.status == 200){
                  if(redirect){
                    this.$router.push(redirect)
                  } else {
                    this.$router.push('/')
                  }
                } else {
                  console.log('error recveived')
                  alert("Fehler bei Username & Passwort bitte erneut eingeben")
                }
            }).catch(err=>{
              console.log('error recveived')
              console.log(err)
              })
        }
  }

}
</script>