import org.gradle.api.initialization.Settings

class RepoConfigExtension {
    Collection modules = []
    Closure execute = { Settings settings ->

        modules.each {
            def info = it.repoModuleInfo('libs')
            def module_file = info.filename.with { endsWith('.groovy') ? delegate : "${delegate}/config.groovy" }
            //def module_path = new File(settings.rootDir, 'repo/' + module_file)
            //def module_path = module_file.repoInfo().repo.module_file
            //module_path.parentFile?.mkdirs()
            def module_path = module_file.repoDownload()
            settings.apply from: module_path

            def module_sub = info.repo.filename.with { substring(0, indexOf('.')) }
            def module_repo = info.repo.module.repo
            def module_name = info.repo.module.name
            if (module_sub != 'config') {
                module_repo = new File(module_repo, module_sub)
                module_name += ".$module_sub"
            }

            settings.include ":$module_name"
            settings.project(":$module_name").projectDir = module_repo

            //println info
            //println "raw:  ${info.repoRaw()}"
            //println "path: $module_path"
            //println "repo: $module_repo"
            //println "name: $module_name"
        }
    }
}

extensions.create('repoConfig', RepoConfigExtension)

//import org.gradle.api.Plugin
//import org.gradle.api.initialization.Settings
//class RepoModulePlugin implements Plugin<Settings> {
//
//    @Override
//    void apply(Settings settings) {
//
//        def modules = settings['repoConfig'].modules
//        println "apply modules $modules"
//        //def execute = settings['repoConfig'].execute
//        //println "apply execute ${execute(settings)}"
//
//    }
//}