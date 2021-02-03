<template>
  <div class="home">
    <h1>Wild West Camp Drigge 2021</h1>
    <v-form>
      <v-container>
        <v-row>
          <v-col cols="12" sm="4">
            <v-btn v-on:click="campTable">Camp</v-btn>
          </v-col>
          <v-col cols="12" sm="4">
            <v-btn v-on:click="personTable">Personen</v-btn>
          </v-col>
        </v-row>
      </v-container>
    </v-form>
  </div>
</template>

<script>
// @ is an alias to /src

export default {
  name: "Home",
  components: {},
  methods: {
    campTable: function () {
      fetch("/api/v1/createPdfTable", {
        method: "GET",
      })
        .then((response) => {
          console.log("response222:");
          console.log(response);
          
          
        })
        .catch((err) => {
          console.log("error recveived");
          console.log(err);
        });
    },
    personTable: function () {
      fetch("/api/v1/createPersonTable", {
        method: "GET",
      })
        .then((response) => {
          console.log("response222:");
          console.log(response);
          
          
        })
        .catch((err) => {
          console.log("error recveived");
          console.log(err);
        });
    },
  },
};
</script>
