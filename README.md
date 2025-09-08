# AndroidZebraTC26
Исходный код андроид приложения Pantes


в приложении реализован: 
* стандартный шаблон проектирования: MVVP 
* оригинальное расширение к TemplatePresenterBinding(Ext.kt) функция setAttribute  в которой для ускорения работы в RecyclerViewHolder
					1. при первом обращении к одной из переменных	из региона templatesAcces создает b добавляет соответствующий View в  TemplatePresenterBinding
					2. при повторном обращении возвращает View



