/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.sbt.jschool.session2;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 */
public class OutputFormatter {
    private PrintStream out;

    public OutputFormatter(PrintStream out){
        this.out = out;
    }

    private DecimalFormat moneyFormat = new DecimalFormat("###,##0.00");
    private DecimalFormat numberFormat = new DecimalFormat("###.###");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private DecimalFormatSymbols dfs = new DecimalFormatSymbols();



    public void output(String[] names, Object[][] data) {
        //TODO: implement me.

        // изменение группового разделителя для чисел
        dfs.setGroupingSeparator(' ');
        numberFormat.setGroupingSize(3);
        numberFormat.setDecimalFormatSymbols(dfs);
        numberFormat.setGroupingUsed(true);

//        // количество столбцов
//        int numberOfColumns = names.length;
//        // количество строк
//        int numberOfRow = data.length;

        // ширина столбцов
        int widths[] = widthsOfColumns(names, data);

        this.out.print(topOfTable(names, widths));
        this.out.print(table(widths, data));
        System.out.print(topOfTable(names, widths));
        System.out.print(table(widths, data));

    }


    // возможные типы данных
    enum typesOfData {
        STRING,
        DATE,
        MONEY,
        NUMBER,
        NULL
    }

    // определение типа данных
    private typesOfData getDataType(Object object){
        if(object instanceof String)
            return typesOfData.STRING;
        else if (object instanceof Date)
            return typesOfData.DATE;
        else if (object instanceof Double || object instanceof Float)
            return typesOfData.MONEY;
        else if (object instanceof Integer)
            return typesOfData.NUMBER;

        return typesOfData.NULL;
    }

    // установка нужного формата представления данных
    private String formatData(Object object){

        typesOfData tod = getDataType(object);
        switch (tod){
            case STRING:
                String str = object.toString();
                if(str.length() == 0)
                    str = "-";
                return str;

            case DATE: return dateFormat.format(object);
            case MONEY: return moneyFormat.format(object);
            case NUMBER: return numberFormat.format(object);
            case NULL: return "-";
        }

        return null;
    }


    // нахождение ширины столбцов. На выходе - массив со значениями
    // ширины столбцов
    private int[] widthsOfColumns(String[] names, Object[][] data){

        int numOfColumns = names.length;
        int dataLength = data.length;
        int len;

        int[] result = new int[numOfColumns];

        // сначала за ширину берем ширину заголовков
        for(int i = 0; i < numOfColumns; i++)
        {
            result[i] = (names[i] == null) ? 1 : names[i].length();
        }
        // затем проверяем по строчкам, поместятся ли в такие ячейки данные
        for(int i = 0; i < data.length; i++){
            for(int j = 0; j < numOfColumns; j++){
                if(data[i][j] != null){
                    len = formatData(data[i][j]).length();
                    if(len > result[j])
                        result[j] = len;
                }
            }
        }
//        //HACK:
//        for(int i = 0; i < numOfColumns; i++)
//            System.out.println("result[" + i + "] = " + result[i]);
        return result;
    }


    // повторение строки
    static private String repeatString(String str, int num){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < num; i++)
            sb.append(str);

        return sb.toString();
    }

    // часть горизонтальной границы
    private String line(int width){
        StringBuilder sb = new StringBuilder();
        sb.append(repeatString("-", width));
        return sb.toString();

    }

    // вертикальная граница
    private String verticalLine(){
        return new String("|");
    }

    // стык границ
    private String delimiter(){
        return new String("+");
    }

    // перенос на новую строку
    private String lineBreak(){
        return new String("\n");
    }

    // горизонтальная граница, с учетом разной ширины столбцов
    private String horizontalLine(int numOfColumns, int[] width)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(delimiter());

        for(int i = 0; i < numOfColumns; i++)
            sb.append(line(width[i]) + delimiter());

        sb.append(lineBreak());

        return sb.toString();
    }

    // строка с названиями столбцов
    private String namesOfColumns(String[] names, int[] width){

        int len = width.length;
        StringBuilder sb = new StringBuilder();

        // массивы для выравнивания
        int[] left = new int[len];
        int[] right = new int[len];
        for(int i = 0; i < len; i++){
            left[i] = (width[i] - names[i].length()) / 2 ;
            right[i] = width[i] - names[i].length() - left[i];
        }
        sb.append(verticalLine());
        for(int i = 0; i < len; i++)
            sb.append(repeatString(" ", left[i])).append(names[i])
                    .append(repeatString(" ", right[i])).append(verticalLine());

        sb.append(lineBreak());
        return sb.toString();
    }

    // строка заголовков с линиями-разделителями
    private String topOfTable(String[] names, int[] width){

        StringBuilder sb = new StringBuilder();
        int len = names.length;

        sb.append(horizontalLine(len, width)).append(namesOfColumns(names, width))
          .append(horizontalLine(len, width));

        sb.length();

        return sb.toString();
    }

    // строка-таблица - склейка всех строк в одну
    private String table(int[] width, Object[][] data){

        String[] raws = raws(width, data);
        int rawsLength = raws.length;
        StringBuilder sb = new StringBuilder();
        for(String x : raws)
            sb.append(x);

        return sb.toString();
    }

    private String[] raws(int[] width, Object[][] data){

        int dataLength = data.length;
        String[] result = new String[dataLength];
        StringBuilder sb = new StringBuilder();

        // выясним тип данных колонок и выведем данные("если, конечно, они у вас есть")
        if(dataLength > 0) {
            typesOfData[] tod = new typesOfData[data[0].length];
            for (int i = 0; i < data[0].length; i++)
                tod[i] = getDataType(data[0][i]);
//            //HACK:
//            System.out.println(tod);

            for (int i = 0; i < dataLength; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    sb.append(verticalLine()).append(cells(width[j], data[i][j], tod[j]));
                    //sb.append(verticalLine()).append(cell(width[j] ,data[i][j]));
                }
                sb.append(verticalLine()).append(lineBreak()).append(horizontalLine(data[i].length, width));
                result[i] = sb.toString();
                sb.setLength(0);
            }
        }
        return result;
    }

    private String cell(int width, Object obj){

        StringBuilder sb = new StringBuilder();
        typesOfData tod = getDataType(obj);
        String data = formatData(obj).toString();
        int len = data.length();

        if(tod == typesOfData.STRING)
            sb.append(data).append(repeatString(" ", width - len));
        else
            sb.append(repeatString(" ", width - len)).append(data);

        return sb.toString();
    }

    private String cells(int width, Object obj, typesOfData tod){

        StringBuilder sb = new StringBuilder();
        String data = formatData(obj).toString();
        int len = data.length();

        if(tod == typesOfData.STRING)
            sb.append(data).append(repeatString(" ", width - len));
        else
            sb.append(repeatString(" ", width - len)).append(data);

        return sb.toString();
    }

//    private void cell(int width, Object obj){
//
//        StringBuilder sb = new StringBuilder();
//        typesOfData tod = getDataType(obj);
//        String int2Str = Integer.toString(width);
//
//        if(tod == typesOfData.STRING)
//            sb.append("%-").append(int2Str).append("s|");
//        else
//            sb.append("%").append(int2Str).append("s|");
//
//        String alignFormat = sb.toString();
//
//        out.format(alignFormat,formatData(obj));
//    }
}


