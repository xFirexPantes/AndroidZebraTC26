package com.example.scanner.models

data class IsolatorReasonsResponse(val found:ArrayList<Item>) {
    data class Item(
        val id:String,
        val label:String
    )
}
//{
//    "found" : [ {
//    "id" : "0",
//    "label" : "Ведется исследование"
//}, {
//    "id" : "6",
//    "label" : "Возврат денежных средств поставщиком"
//}, {
//    "id" : "10",
//    "label" : "Замена брака из излишков"
//}, {
//    "id" : "1",
//    "label" : "Замена элементов"
//}, {
//    "id" : "9",
//    "label" : "Излишки"
//}, {
//    "id" : "5",
//    "label" : "Перенос в автоматический элемент"
//}, {
//    "id" : "4",
//    "label" : "Перенос в ручной элемент"
//}, {
//    "id" : "2",
//    "label" : "Тест паяемости"
//}, {
//    "id" : "7",
//    "label" : "Утилизация элемента"
//}, {
//    "id" : "8",
//    "label" : "Функциональный контроль"
//} ]
//}