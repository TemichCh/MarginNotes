package com.example.notesdemo

import android.app.Application
import com.example.notesdemo.dao.NotesLocalDb
import com.example.notesdemo.dao.NotesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

// FIXME: видимо глобальное замечание - надо сделать автоформатирование кода, пробелы где полагается отстутвуют.
//  в меню правой кнопки - Reformat Code (на директории с сорсами). и при работе с кодом хоткеем вызывать эту функцию
// TODO чтобы меньше запариваться о форматировании и иметь гарантию что все в коде ок -
//  подключи https://github.com/detekt/detekt как gradle плагин, и в IDEA скачай тоже плагин этот
//  тогда будет сразу подсказка где что не так, а также градл при выполнении таски detekt будет
//  выводить ошибки
class NotesApplication:Application() {
    // FIXME: раз мы создали application scope, то мы должны его еще и отменить, в момент onDestroy
    private val applicationScope = CoroutineScope(SupervisorJob())

    // FIXME раз на уровне Application объявлен синглтон (в виде lazy) то нет смысла в самом
    //  NotesLocalDb делать getInstance - лучше на фабричную функцию заменить это.
    //  пусть контроль один инстанс или не один делает не класс бд
    private val database by lazy { NotesLocalDb.getInstance(this,applicationScope) }
    // FIXME как я понимаю это является некоторым DI для всего приложения. Но на местах получения
    //  этого репозитория, который для всех экранов единый, получается мы должны кастить Application
    //  к своему типу и размазывать это по всему коду приложения - плохо, потом может в ногу выстрелить
    //  если например будет отдельный buildFlavor где свой класс Application будет.
    //  Советую сделать функцию расширение Application.getRepository() которая внутри будет каст иметь,
    //  но все приложение будет завязано на эту функцию, а не в каждом месте будет каст.
    // TODO для знакомство с более общепринятым подходмо внедрения зависимостей -
    //  надо заинтегрировать Hilt https://developer.android.com/training/dependency-injection/hilt-android
    val repository by lazy { NotesRepository(database.localNotesDao()) }

    //??? У Application не нашел onDestroy
    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
}

//??? пошел по простому пути пока
fun Application.getNotesRepository(): NotesRepository {
    return (this as NotesApplication).repository
}

// вариант с экстеншен функцией

//fun Application.getNotesRepository(): NotesRepository {
//    return (this as NotesApplication).repository
//}

// вариант с DI интерфейсом для всех будущих зависимостей тоже - проще добавлять новые зависимости

//interface DependenciesContainer {
//    val notesRepository: NotesRepository
//}
//
//class NotesApplication: Application(), DependenciesContainer {
//    private val applicationScope = CoroutineScope(SupervisorJob())
//
//    private val database by lazy { NotesLocalDb.getInstance(this,applicationScope) }
//
//    override val notesRepository by lazy { NotesRepository(database.LocalNotesDao()) }
//}
//
//val Application.dependencies: DependenciesContainer get() = (this as DependenciesContainer)
