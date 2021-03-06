package com.shuyu.github.kotlin.model.conversion

import android.content.Context
import com.shuyu.github.kotlin.R
import com.shuyu.github.kotlin.common.utils.CommonUtils
import com.shuyu.github.kotlin.model.bean.FileModel
import com.shuyu.github.kotlin.model.bean.Repository
import com.shuyu.github.kotlin.model.bean.TrendingRepoModel
import com.shuyu.github.kotlin.model.ui.FileUIModel
import com.shuyu.github.kotlin.model.ui.ReposUIModel

/**
 * 仓库相关实体转换
 * Created by guoshuyu
 * Date: 2018-10-29
 */
object ReposConversion {

    fun trendToReposUIModel(trendModel: TrendingRepoModel): ReposUIModel {
        val reposUIModel = ReposUIModel()
        reposUIModel.hideWatchIcon = true
        reposUIModel.ownerName = trendModel.name
        reposUIModel.ownerPic = trendModel.contributors[0]
        reposUIModel.repositoryDes = trendModel.description
        reposUIModel.repositoryName = trendModel.reposName
        reposUIModel.repositoryFork = trendModel.forkCount
        reposUIModel.repositoryStar = trendModel.starCount
        reposUIModel.repositoryWatch = trendModel.meta
        reposUIModel.repositoryType = trendModel.language
        return reposUIModel
    }

    fun reposToReposUIModel(context: Context, repository: Repository?): ReposUIModel {
        val reposUIModel = ReposUIModel()
        reposUIModel.hideWatchIcon = true
        reposUIModel.ownerName = repository?.owner?.login ?: ""
        reposUIModel.ownerPic = repository?.owner?.avatarUrl ?: ""
        reposUIModel.repositoryDes = repository?.description ?: ""
        reposUIModel.repositoryName = repository?.name ?: ""
        reposUIModel.repositoryFork = repository?.forksCount?.toString() ?: ""
        reposUIModel.repositoryStar = repository?.stargazersCount?.toString() ?: ""
        reposUIModel.repositoryWatch = repository?.watchersCount?.toString() ?: ""
        reposUIModel.repositoryType = repository?.language ?: ""
        reposUIModel.repositorySize = (((repository?.size
                ?: 0) / 1024.0)).toString().substring(0, 3) + "M"
        reposUIModel.repositoryLicense = repository?.license?.name ?: ""


        val createStr = if (repository != null && repository.fork)
            context.getString(R.string.forked_at) + (repository.parent?.name ?: "") + "\n"
        else
            context.getString(R.string.created_at) + CommonUtils.getDateStr(repository?.createdAt) + "\n"

        reposUIModel.repositoryAction = createStr
        reposUIModel.repositoryIssue = repository?.openIssuesCount?.toString() ?: ""
        return reposUIModel
    }


    fun fileListToFileUIList(list: ArrayList<FileModel>): ArrayList<Any> {
        val result = ArrayList<Any>()
        val dirs = ArrayList<Any>()
        val files = ArrayList<Any>()

        list.forEach {
            val fileUIModel = FileUIModel()
            fileUIModel.title = it.name ?: ""
            fileUIModel.type = it.type ?: ""
            if (it.type == "file") {
                fileUIModel.icon = "{GSY-REPOS_ITEM_FILE}"
                fileUIModel.next = ""
                files.add(fileUIModel)
            } else {
                fileUIModel.icon = "{ion-android_folder_open}"
                fileUIModel.next = "{GSY-REPOS_ITEM_NEXT}"
                dirs.add(fileUIModel)
            }
        }
        result.addAll(dirs)
        result.addAll(files)
        return result
    }

}