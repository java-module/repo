def repoInfo = 'github/wittyneko/ktx-base/master/base.groovy'.gitRepoInfo(null, null, true, 'modules')
def repoZip = repoInfo.gitRepoZip()

def module_tag = repoInfo.tags
def module_name = "${repoInfo.module.name}.base"
def module_repo = new File(repoInfo.module.repo, 'base')
def module_cache_dir = new File(repo_caches, module_name.replace('.', '/'))
def module_cache_file = new File(module_cache_dir, "${module_tag}.zip")

//println "$module_name info: " + repoInfo
//println "$module_name zip: " + repoZip
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
        new File(module_cache_dir, "$module_tag/ktx-base-$module_tag/base").copyTo(it)
    }
}
