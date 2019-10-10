package pt.ul.fc.di.lasige.a4tvapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Daniel Costa on 01/02/2018.
 */

public class A4TVAdaptationAndTutorialDialogs {
    private Context context;
    private boolean experiencedUser = false;
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;

    public A4TVAdaptationAndTutorialDialogs(Context context){
        this.context = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPrefs.edit();
    }

    public void startTutorial(){
        createTutorialDialog1("Primeira utilização", "Esta aplicação é capaz de se conectar à sua televisão e servir de leitor de ecrã, tal como o talkback no seu telemóvel. Antes de começar a utilizá-la, necessitamos de conhecer as suas capacidades para adaptarmos a aplicação às suas necessidades. Para continuar seleccione Seguinte.");
    }


    public AlertDialog.Builder createAdaptationDialog(String title, String message, final String propToAdapt, final String valueToAdapt){
        DialogBuilder bld = new DialogBuilder();
        AlertDialog.Builder builder = bld.createDialog(context, title, message);


        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        })
        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // accepted
                //editor.putString("reading_preference", "1");
                editor.putString(propToAdapt, valueToAdapt);
                editor.commit();

                //update values
                /*
                readingMode = Integer.parseInt(sharedPrefs.getString("reading_preference", "1"));
                interactMode = Integer.parseInt(sharedPrefs.getString("interact_preference", "1"));
                focusMode = Integer.parseInt(sharedPrefs.getString("focus_preference", "1"));*/
                }
        }).show();


                return builder;

    }

    public AlertDialog.Builder createSuggestionDialog(String title, String message){
        DialogBuilder bld = new DialogBuilder();
        AlertDialog.Builder builder = bld.createDialog(context, title, message);

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // accepted

            }
        })
                .show();



        return builder;

    }


    private void createTutorialDialog1(String title, String message){

        DialogBuilder bld = new DialogBuilder();
        AlertDialog.Builder builder = bld.createDialog(context, title, message);

        builder.setPositiveButton("Seguinte", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // accepted
                createTutorialDialog2("Caracteristicas do Utilizador", "De modo a obter uma experiência satisfatória. Indique qual a opção que caracteriza a sua deficiência visual.", "Totalmente Cego","Algum resíduo visual","user_type", "1", "2");
            }
        })
                .show();

    }

    private void createTutorialDialog2(String title, String message, String op1, String op2, final String propToAdapt, final String value1, final String value2){
        DialogBuilder bld = new DialogBuilder();
        AlertDialog.Builder builder = bld.createDialog(context, title, message);

        builder.setPositiveButton(op1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // accepted
                editor.putString(propToAdapt, value1);
                editor.commit();

                //update values
                /*readingMode = Integer.parseInt(sharedPrefs.getString("reading_preference", "1"));
                interactMode = Integer.parseInt(sharedPrefs.getString("interact_preference", "1"));
                focusMode = Integer.parseInt(sharedPrefs.getString("focus_preference", "1"));*/

                createTutorialDialog3("Experiência com leitores de ecrã", "Gostariamos de saber um pouco sobre a sua experiência. Há quanto tempo usa leitores de ecrã nos seus dispositivos?", "Mais de 2 anos","Menos de 2 anos","reading_preference", "2", "1");
            }
        })
                .setNegativeButton(op2, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        // accepted

                        //editor.putString("reading_preference", "1");
                        editor.putString(propToAdapt, value2);
                        editor.commit();

                        //update values
                        /*
                        readingMode = Integer.parseInt(sharedPrefs.getString("reading_preference", "1"));
                        interactMode = Integer.parseInt(sharedPrefs.getString("interact_preference", "1"));
                        focusMode = Integer.parseInt(sharedPrefs.getString("focus_preference", "1"));
                        */
                        createTutorialDialog3("Experiência com leitores de ecrã", "Gostariamos de saber um pouco sobre a sua experiência. Há quanto tempo usa leitores de ecrã nos seus dispositivos?", "Mais de 2 anos","Menos de 2 anos","reading_preference", "2", "1");

                    }
                })
                .show();

    }

    private void createTutorialDialog3(String title, String message, String op1, String op2, final String propToAdapt, final String value1, final String value2){
        DialogBuilder bld = new DialogBuilder();
        AlertDialog.Builder builder = bld.createDialog(context, title, message);

        builder.setPositiveButton(op1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // accepted
                editor.putString(propToAdapt, value1);
                editor.commit();
                experiencedUser= true;
                //update values
                /*readingMode = Integer.parseInt(sharedPrefs.getString("reading_preference", "1"));
                interactMode = Integer.parseInt(sharedPrefs.getString("interact_preference", "1"));
                focusMode = Integer.parseInt(sharedPrefs.getString("focus_preference", "1"));*/
                createTutorialDialog4("Interface", "O primeiro passo para poder usar esta aplicação é ativar o botão Ligar para conectar o " +
                        "telemóvel à sua televisão. Assim que essa ligação seja feita terá acesso a 3 botões no topo. O botão ler ecrã, faz a leitura integral da aplicação de TV. " +
                                "O botão Localizar, situa o utilizador onde está o foco. O botão comando de voz aciona o reconhecimento de voz. De seguida, encontram-se" +
                                " os botões direcionais para enviar os comandos para a televisão e o botão ok.");

            }
        })
                .setNegativeButton(op2, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        // accepted

                        //editor.putString("reading_preference", "1");
                        editor.putString(propToAdapt, value2);
                        editor.commit();

                        //update values
                        /*
                        readingMode = Integer.parseInt(sharedPrefs.getString("reading_preference", "1"));
                        interactMode = Integer.parseInt(sharedPrefs.getString("interact_preference", "1"));
                        focusMode = Integer.parseInt(sharedPrefs.getString("focus_preference", "1"));
                        */
                        createTutorialDialog4("Interface", "O primeiro passo para poder usar esta aplicação é ativar o botão Ligar para conectar o " +
                                "telemóvel à sua televisão. Assim que essa ligação seja feita terá acesso a 3 botões no topo. O botão ler ecrã, faz a leitura integral da aplicação de TV. " +
                                "O botão Localizar, situa o utilizador onde está o foco. O botão comando de voz aciona o reconhecimento de voz. De seguida, encontram-se" +
                                " os botões direcionais para enviar os comandos para a televisão e o botão ok.");

                    }
                })
                .show();

    }

    private void createTutorialDialog4(String title, String message){

        DialogBuilder bld = new DialogBuilder();
        AlertDialog.Builder builder = bld.createDialog(context, title, message);

        builder.setPositiveButton("Seguinte", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // accepted
                createTutorialDialog5("Multimodalidade", "É possível interagir de diferentes maneiras. Pode ativar os botões da aplicação móvel através do TalkBack." +
                        " Ou deslisando dois dedos no ecrã, pode enviar os comandos direcionais directamente para a sua televisão. Pode ainda usar a sua voz, pressionando com dois dedos" +
                        " no ecrã ou usando o botão comando de voz, espere pelo sinal e de seguida fale o comando, Baixo, Cima, Localizar etc.");
            }
        })
                .show();

    }

    private void createTutorialDialog5(String title, String message){

        DialogBuilder bld = new DialogBuilder();
        AlertDialog.Builder builder = bld.createDialog(context, title, message);

        builder.setPositiveButton("Seguinte", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // accepted
                String recommendation = "";
                /*if(experiencedUser){
                    recommendation = "A aplicação recomenda usar o modo Conciso, aceita?";
                }else{
                    recommendation = "A aplicação recomenda usar o modo Detalhado, aceita?";
                }*/
                recommendation = "Começará por usar o modo Detalhado.";
                createTutorialDialog6("Modo de Feedback", "É possível apresentar a informação do que se passa no ecrã da televisão de duas maneiras. O modo detalhado apresenta-lhe " +
                        "mais contexto sobre a interface da aplicação TV, como por exemplo, todas as opções existentes no menu onde se encontra. Por outro lado o modo conciso, remove essa informação" +
                        " para uma interação mais rápida. " + recommendation);
            }
        })
                .show();

    }

    private void createTutorialDialog6(String title, String message){

        DialogBuilder bld = new DialogBuilder();
        AlertDialog.Builder builder = bld.createDialog(context, title, message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // accepted
                editor.putString("reading_preference", A4TVMobileClient.VERBOSE+"");
                editor.putString("focus_preference", A4TVMobileClient.FOCUS_SIBLINGS+"");
                editor.commit();
                createTutorialDialogFinish("Tutorial Concluído", "Obrigado por responder a estas questões. Pode utilizar livremente a aplicação.");
            }
        })
                /*.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(experiencedUser){
                            editor.putString("reading_preference", "1");
                            editor.commit();
                        }else{
                            editor.putString("reading_preference", "2");
                            editor.commit();
                        }

                        createTutorialDialogFinish("Tutorial Concluído", "Obrigado por responder a estas questões. Pode utilizar livremente a aplicação.");
                    }
                })*/
                .show();

    }


    private void createTutorialDialogFinish(String title, String message){

        DialogBuilder bld = new DialogBuilder();
        AlertDialog.Builder builder = bld.createDialog(context, title, message);

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // accepted

            }
        })
                .show();

    }


}
