String.metaClass.gitRepoInfo = { String name = null, String tag = null, boolean overHost = true, String repo = 'repo' ->
    def info_index = split('/') as String[]
    def info_host
    switch (info_index[0]) {
        case 'github':
            info_host = 'github.com'
            break
        case 'gitlab':
            info_host = 'gitlab.com'
            break
        case 'gitee':
            info_host = 'gitee.com'
            break
        default:
            info_host = info_index[0]
    }
    def info_module = ['index': info_index.with { dropRight(length - 3) }]
    def info = [
            'host'           : overHost ? info_host : info_index[0],
            'user'           : info_index[1],
            'repository'     : info_index[2],
            'user_repository': "${info_index[1]}/${info_index[2]}",
            'tags'           : "${tag != null ? tag : 3 < info_index.length ? info_index[3] : 'master'}",
            'filename'       : "${name != null ? name : 4 < info_index.length ? info_index.with { drop(4) }.join('/') : 'config.groovy'}",
            'index'          : info_index,
            'module'         : info_module,
    ]
    info_module << [
            'name': info_module.index.join('.'),
            'repo': new File(rootDir, "$repo/${info_module.index.join('/')}"),
    ]
    info
}

Map.metaClass.gitRepoRaw = { String name = null, String tag = null ->
    //delegate.containsKey('repo') ? {
    //    def module_path = repo.with {
    //        "$module_type/$host/$user_repository/${tag != null ? tag : tags}/${name != null ? name : filename}"
    //    }
    //    "https://$host/$user_repository/raw/${tags}/${module_path}"
    //} : {
    //    "https://$host/$user_repository/raw/${tag != null ? tag : tags}/${name != null ? name : filename}"
    //}
    "https://$host/$user_repository/raw/${tag != null ? tag : tags}/${name != null ? name : filename}"
}

String.metaClass.gitRepoRaw = { String name = null, String tag = null ->

    gitRepoInfo(name, tag).gitRepoRaw()
}

Map.metaClass.gitRepoZip = { String tag = null ->
    def archive
    switch (host) {
        case 'gitee.com':
            archive = "https://$host/$user_repository/repository/archive/${tag != null ? tag : tags}.zip"
            break
        default:
            archive = "https://$host/$user_repository/archive/${tag != null ? tag : tags}.zip"
    }
    archive
}

String.metaClass.gitRepoZip = { String tag = null ->

    gitRepoInfo(null, tag).gitRepoZip()
}

String.metaClass.repoModuleInfo = { String type = null, String name = null, String tag = null ->
    def index = split('/') as String[]
    if (type != null) index = [type].toArray() + index
    def name_index = index.drop(1)
    def info_repo = [
            'module_type': index[0],
            'module_name': name_index.join('/'),
    ]
    info_repo << info_repo.module_name.gitRepoInfo(name, tag, false, 'modules')
    def path = "github/java-module/repo/master/${index.join('/')}"
    def info = path.gitRepoInfo() << [repo: info_repo]
    info
}

String.metaClass.repoInfo = { String type = null, String name = null, String tag = null ->
    def index = split('/') as String[]
    if (type != null) index = [type].toArray() + index
    def name_index = index.drop(1)
    def info_repo = [
            'module_type': index[0],
            'module_name': name_index.join('/'),
            'module_file': new File(rootDir, "repo/${index.join('/')}"),
    ]
    def path = "github/java-module/repo/master/${index.join('/')}"
    def info = path.gitRepoInfo(null, tag) << [repo: info_repo]
    info
}

String.metaClass.repoDownload = {
    def info = repoInfo()
    def info_raw = info.gitRepoRaw()
    def info_file = info.repo.module_file
    info_file.with {
        if (!exists()) {
            parentFile?.mkdirs()
            new URL(info_raw).download(it)
        }
    }
    info_file
}

Map.metaClass.repoRaw = { String name = null, String tag = null ->

    def module_path = repo.with {
        "$module_type/$host/$user_repository/${tag != null ? tag : tags}/${name != null ? name : filename}"
    }
    "https://$host/$user_repository/raw/${tags}/${module_path}"
}

String.metaClass.repoRaw = {

    "https://github.com/java-module/repo/raw/master/$delegate"
}


ext {
    user_home = System.properties['user.home']
    repo_caches = "${user_home}/.repo_modules/caches"
}