def cache = "${user_home}/.repo/cache"

def repoInfo = 'github/wittyneko/gradle-tools/master/config.groovy'.gitRepoInfo(null, null, true, 'modules')
def repoZip = repoInfo.gitRepoZip()

def module_name = repoInfo.module.name
def module_repo = repoInfo.module.repo as File
def module_cache_dir = new File(cache, module_name.replace('.', '/'))
def module_cache_file = new File(module_cache_dir, 'archive.zip')

//println "$module_name info: " + repoInfo
//println "$module_name zip: " + repoZip
//println "$module_name repo: " + module_repo
//println "$module_name cache: " + module_cache_dir

// 下载缓存
module_cache_file.with {
    if (!exists()) {
        parentFile?.mkdirs()
        new URL(repoZip).download(it)
        unzip(new File(module_cache_dir, 'archive'), true)
    }
}

// 复制到项目
module_repo.with {
    if (!exists()) {
        parentFile?.mkdirs()
        new File(module_cache_dir, 'archive/gradle-tools-master').copyTo(it)
    }
}
