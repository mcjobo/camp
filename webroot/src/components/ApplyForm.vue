<template>
    <v-form>
        <v-container>

            <p class="text-h3">Person zum Anmelden</p>
            <v-row>
                <v-col cols="12" sm="12">
                    <v-autocomplete label="Stamm" :items="outpostList" v-model="signinData.outpostValue"></v-autocomplete>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="6">
                    <v-text-field label="Vorname" v-model="signinData.firstName"></v-text-field>
                </v-col>
                <v-col cols="12" sm="6">
                    <v-text-field label="Nachname" v-model="signinData.lastName"></v-text-field>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="12">
                    <v-text-field label="Strasse" v-model="signinData.street"></v-text-field>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="6">
                    <v-text-field label="PLZ" v-model="signinData.zip"></v-text-field>
                </v-col>
                <v-col cols="12" sm="6">
                    <v-text-field label="Ort" v-model="signinData.city"></v-text-field>
                </v-col>
            </v-row>
            <!--<v-row>
                <v-col cols="12" sm="6">
                    <v-menu v-model="fromDateMenu" :close-on-content-click="false" :nudge-right="40" transition="scale-transition"
                    offset-y max-width="290px"  min-width="290px">
            <template v-slot:activator="{ on }">
              <v-text-field
                label="Geburtsdatum" append-icon="event" readonly :value="fromDateDisp" v-on="on"/>
            </template>
            <v-date-picker v-model="fromDateVal" no-title  @input="fromDateMenu = false" :min="minDate"/>
          </v-menu>
                </v-col>
                <v-col cols="12" sm="6">
                    <v-text-field label="Handy"></v-text-field>
                </v-col>
            </v-row>-->
            <v-row>
                <v-col cols="12" sm="6">
                    <v-combobox label="Team" v-model="signinData.teamValue" :items="teamList"/>
                </v-col>
                <v-col cols="12" sm="6">
                    <v-radio-group row>
                        <v-radio label="Kundschafter"/>
                        <v-radio label="Pfadfinder"/>
                        <v-radio label="Pfadranger"/>
                    </v-radio-group>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="12">
                    <v-autocomplete label="Vorcamp" v-model="signinData.preCampValue" :items="preCampList" chips multiple/>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="12">
                    <v-autocomplete label="Verpflegung" v-model="signinData.foodPreferencesValue" :items="foodPreferencesList" chips multiple/>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="12">
                    <v-text-field label="Lebensmittelunverträglichkeiten" v-model="signinData.foodAllergies"></v-text-field>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="12">
                    <v-text-field label="Allergien" v-model="signinData.allergies"></v-text-field>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="12">
                    <v-text-field label="Gesundh. Einschr. / Medikation" v-model="signinData.medicine"></v-text-field>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="12">
                    <v-text-field label="Notfallkontakt" v-model="signinData.emergencyContact"></v-text-field>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="4">
                    <v-text-field label="T-Shirt"  v-model="signinData.tshirt"></v-text-field>
                </v-col>
                <v-col cols="12" sm="4">
                    <v-checkbox label="Badeerlaubnis" v-model="signinData.swimmingPermit"/>
                </v-col>
                <v-col cols="12" sm="4">
                    <v-checkbox label="Bildrechte" v-model="signinData.imageRights"/>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="12">
                    <v-textarea  label="Kommentar" hint="toller Text" v-model="signinData.comment"></v-textarea> 
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" sm="12">
                    <v-btn v-on:click="signinPerson">OK</v-btn>
                </v-col>
            </v-row>
        </v-container>
    </v-form>
</template>
<script>
export default {
    name: 'ApplyForm',
    data:()=>({
        outpostList: ['375 Rostock 1', '556 Rostock 2'],
        teamList: ['Seeadler', 'Fischadler', 'freie Mitarbeiter', 'Familie'],
        preCampList: ['Freitag(18.06)', 'Samstag(18.06)', 'Sonntag(20.06)'],
        foodPreferencesList: ['vegetarisch', 'laktosefrei', 'glutenfrei'],
        tshirtList: ['kein T-Shirt', '104', '116', '128', '140', '152', '164', 'S', 'M', 'L', 'XL', 'XXL'],
        signinData: {
            outpostValue: '',
            firstName: '',
            lastName: '',
            street: '',
            zip: '',
            city:'',
            teamValue: '',
            preCampValue: '',
            foodPreferencesValue: '',
            foodAllergies: '',
            allergies: '',
            medicine: '',
            emergencyContact: '',
            tshirt: '',
            swimmingPermit: '',
            imageRights: '',
            comment: '',
        },

    }),
    methods:{
        signinPerson: function(event){
            console.log(this, event)
            console.log(this.signinData)
            fetch("http://localhost:8080/api/v1/postSigninToGroup", {
                "method": "POST",
                 "body": JSON.stringify(this.signinData)
            }).then(response =>{
                console.log('response:')
                console.log(response)
            }).catch(err=>{console.log(err)})
        }
    },
    
}
</script>