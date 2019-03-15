package com.kuzheevadel.photogallery

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.res.Resources
import android.os.AsyncTask
import android.util.Log
import com.kuzheevadel.photogallery.utils.checkForNewPhoto

class PullServiceJobService: JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.i("PollService", "In OnStartJob")
        checkForNewPhoto(resources, applicationContext)
        return false
    }

    companion object {

        @JvmStatic
        fun startJob(context: Context, job_id: Int) {
            val scheduler: JobScheduler =
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            val jobInfo: JobInfo = JobInfo.Builder(
                    job_id, ComponentName(context, PullServiceJobService::class.java))
                    .setPeriodic(15 * 60 * 1000)
                    .setPersisted(true)
                    .build()
            scheduler.schedule(jobInfo)

            QueryPreferences.setAlarmOn(context, true)

            Log.i("PollService", "Job is started")
        }

        @JvmStatic
        fun stopJob(context: Context, job_id: Int) {
            val scheduler: JobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            scheduler.cancel(job_id)

            QueryPreferences.setAlarmOn(context, false)

            Log.i("PollService", "Job is canceled")
        }

        @JvmStatic
        fun isAlarmOn(context: Context, job_id: Int): Boolean {
            var hasBeenScheduled = false

            val jobScheduler: JobScheduler =
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            for (jobInfo in jobScheduler.allPendingJobs) {
                if (jobInfo.id == job_id) {
                    hasBeenScheduled = true
                }
            }
            return hasBeenScheduled
        }
    }
}
