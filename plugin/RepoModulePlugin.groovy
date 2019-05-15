import org.gradle.api.initialization.Settings

class RepoConfigExtension {
    Collection modules = []
    Closure defDownload = { String module, String repo_caches ->
        def repoInfo = module.gitRepoInfo(null, null, true, 'modules')
        def repoZip = repoInfo.gitRepoZip()

        def module_tag = repoInfo.tags //分支.tag
        def module_parent = repoInfo.repository //仓库名
        def filename = repoInfo.filename
        def module_sub = filename.with { substring(0, lastIndexOf('.')) } //子模块
        def module_name = module_sub.with { 'config' == it ? repoInfo.module.name : "${repoInfo.module.name}.$it"} //library 项目名称
        def module_repo = module_sub.with { 'config' == it ? repoInfo.module.repo : new File(repoInfo.module.repo, module_sub) } //library 项目目录
        def module_cache_dir = new File(repo_caches, module_name.replace('.', '/')) //缓存目录
        def module_cache_file = new File(module_cache_dir, "${module_tag}.zip") //缓存文件名

        //println "$module_name info: " + repoInfo
        //println "$module_name zip: " + repoZip
        //println "$module_name tag: " + module_tag
        //println "$module_name sub: " + module_sub
        //println "$module_name repo: " + module_repo
        //println "$module_name cache: " + module_cache_dir

        // 下载缓存
        module_cache_file.with {
            if (!exists()) {
                parentFile?.mkdirs()
                new URL(repoZip).download(it)
                unzip(new File(module_cache_dir, module_tag), true)
            }
        }

        // 解压
        new File(module_cache_dir, module_tag).with {
            if (!exists()) {
                parentFile?.mkdirs()
                module_cache_file.unzip(it, true)
            }
        }

        // 复制到项目
        module_repo.with {
            if (!exists()) {
                parentFile?.mkdirs()
                new File(module_cache_dir, "$module_tag/$module_parent-$module_tag${module_sub.with { 'config' == it ? '' : "/$it"}}").copyTo(it)
            }
        }
    }

    Closure execute = { Settings settings ->

        modules.each { module ->
            def module_info = module.repoModuleInfo('libs')
            def module_file = module_info.filename.with { endsWith('.groovy') ? delegate : "${delegate}/config.groovy" }

            //settings.apply from: module_file.repoDownload()
            //defDownload(module, settings.repo_caches)
            def info = module_file.repoInfo()
            def info_raw = info.gitRepoRaw()
            def info_file = info.repo.module_file
            info_file.with {
                if (!exists()) {
                    parentFile?.mkdirs()
                    if (new URL(info_raw).download(it) == 200) {
                        settings.apply from: info_file
                    } else {
                        defDownload(module, settings.repo_caches)
                    }
                } else {
                    settings.apply from: info_file
                }
            }


            def module_sub = module_info.repo.filename.with { substring(0, indexOf('.')) }
            def module_repo = module_info.repo.module.repo
            def module_name = module_info.repo.module.name
            if (module_sub != 'config') {
                module_repo = new File(module_repo, module_sub)
                module_name += ".$module_sub"
            }

            settings.include ":$module_name"
            settings.project(":$module_name").projectDir = module_repo

            //println module_info
            //println "raw:  ${module_info.repoRaw()}"
            //println "path: $info_file"
            //println "repo: $module_repo"
            //println "name: $module_name"
        }
    }
}

extensions.create('repoConfig', RepoConfigExtension)
