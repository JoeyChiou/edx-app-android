package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.base.CourseDetailBaseFragment;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.event.FlyingMessageEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.task.EnrollForCourseTask;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectExtra;

public class CourseDetailActivity extends BaseSingleFragmentActivity {

    public CourseDetail courseDetail;
    boolean emailOptIn = true;

    public static Intent newIntent(@NonNull Context context, @NonNull CourseDetail courseDetail) {
        return new Intent(context, CourseDetailActivity.class)
                .putExtra(CourseDetailFragment.COURSE_DETAIL, courseDetail);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        courseDetail = extras.getParcelable("course_detail");

        setTitle(R.string.course_detail_title);
        //environment.getSegment().trackScreenView(ISegment.Screens.???? + CourseDetail.course_id); //TODO Course Detail Screen, figure out what information to send.
    }

    @Override
    public Fragment getFirstFragment() {
        return new CourseDetailFragment();
    }

    public void enrollButtonClicked(View view) {
        EnrollForCourseTask enrollForCourseTask = new EnrollForCourseTask(CourseDetailActivity.this,
                courseDetail.course_id, emailOptIn) {
            @Override
            public void onSuccess(Boolean result) {
                if(result!=null && result) {
                    logger.debug("Enrollment successful");
                    //If the course is successfully enrolled, send a broadcast
                    // to close the FindCoursesActivity
                    Intent intent = new Intent();
                    intent.putExtra("course_id", courseDetail.course_id);
                    sendBroadcast(intent);

                    // show flying message about the success of Enroll

                    EnrolledCoursesResponse course = environment.getServiceManager().getCourseById(courseDetail.course_id);
                    String msg;
                    if (course == null || course.getCourse() == null ) {
                        // this means, you were not already enrolled to this course
                        msg = String.format("%s", context.getString(R.string.you_are_now_enrolled));
                    }else{
                        // this means, you were already enrolled to this course
                        msg = String.format("%s", context.getString(R.string.already_enrolled));
                    }
                    EventBus.getDefault().postSticky(new FlyingMessageEvent(msg));
                    Toast.makeText(getApplicationContext(), "Enroll Button Clicked", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getApplicationContext(), "200 but result is empty?", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                logger.debug("Error during enroll api call");
                Toast.makeText(getApplicationContext(), "not 200 from enrollment api", Toast.LENGTH_SHORT).show();
            }
        };
        enrollForCourseTask.execute();
    }
}
