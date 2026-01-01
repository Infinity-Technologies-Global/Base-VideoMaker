package com.vanvatcorporation.doubleclips.activities.main;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.TemplatePreviewActivity;
import com.vanvatcorporation.doubleclips.externalUtils.Random;
import com.vanvatcorporation.doubleclips.helper.ImageHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TemplateAreaScreen extends BaseAreaScreen {
    public List<TemplateData> templateList;
    public RecyclerView templateRecyclerView;
    public TemplateDataAdapter templateAdapter;
    public SwipeRefreshLayout templateSwipeRefreshLayout;



    public TemplateAreaScreen(Context context) {
        super(context);
    }

    public TemplateAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TemplateAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TemplateAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void init() {
        super.init();

        templateRecyclerView = findViewById(R.id.templateRecyclerView);
        templateSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);




        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        templateRecyclerView.setLayoutManager(layoutManager);


        templateList = new ArrayList<>();
        templateAdapter = new TemplateDataAdapter(getContext(), templateList);
        templateRecyclerView.setAdapter(templateAdapter);

        templateSwipeRefreshLayout.setOnRefreshListener(this::reloadingProject);


    }



    public void addTemplate(TemplateData data)
    {
        templateList.add(data);
        templateAdapter.notifyItemInserted(templateList.size() - 1);
    }

    public void fetchTemplate() {

        // TODO: Fetch from real server. This is for example purpose.
        TemplateData data = new TemplateData(
                "viet2007ht",
                "mkr5r-SDfve6",
                "Tớ yêu cậu Template",
                "Template tớ yêu cậu là template đầu tiên trong hệ sinh thái. Ra mắt vào ngày 28/12/2025, Template tớ yêu cậu đã đánh dấu sự ra đời của hệ thống mẫu chỉnh sửa video mã nguồn mở.",
                "-i \"<editable-video-0>\" -y \"<output.mp4>\"",
                "https://app.vanvatcorp.com/doubleclips/templates/viet2007ht/mkr5r-SDfve6/preview.png",
                "https://app.vanvatcorp.com/doubleclips/templates/viet2007ht/mkr5r-SDfve6/preview.mp4",
                new Date().getTime(), 8032007, 1, new String[]{});
        addTemplate(data);
        TemplateData data1 = new TemplateData(
                "viet2007ht",
                "dvtr5_gdGER5",
                "Recap 2025 Template",
                "Template Recap 2025. Ra mắt vào ngày 01/01/2026, Template recap 2025 đã đánh dấu sự khởi đầu mới của hệ thống mẫu chỉnh sửa video mã nguồn mở.",
                "-f lavfi -i color=c=black:s=1920x1080:r=30 -t 22.319744 -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-0>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-1>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-2>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-3>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-4>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-5>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-6>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-7>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-8>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-9>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-10>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-11>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-12>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-13>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-14>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-15>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-16>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-17>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-18>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-19>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-20>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-21>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-22>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-23>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-24>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-25>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-26>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-27>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-28>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-29>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-30>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -i \"<editable-video-31>\" -i \"<static-thucgiac.mp3>\" -f lavfi -i \"nullsrc=size=1920x1080:rate=30,format=rgba\" -filter_complex \"[0:v]trim=duration=25.319744,setpts=PTS-STARTPTS[base];\n" +
                        "[1:v]trim=duration=6.7697334,setpts=PTS-STARTPTS+0.0/TB[trans-1];\n" +
                        "[2:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,6.39),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/6.346478,0,1),0.0)':ow=rotw('if(between(t,0.04352188,6.39),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/6.346478,0,1),0.0)'):oh=roth('if(between(t,0.04352188,6.39),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/6.346478,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,6.39),1.2+(1.0-1.2)*clip((it-0.04352188)/6.346478,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+0.0/TB',trim=duration=6.7697334,tpad=stop_mode=clone:stop_duration=3.7697332[video-1];\n" +
                        "[trans-1][video-1]overlay='if(between(t,0.04352188,6.39),0.0+(0.0-0.0)*clip((t-0.04352188)/6.346478,0,1),0.0)':'if(between(t,0.04352188,6.39),0.0+(0.0-0.0)*clip((t-0.04352188)/6.346478,0,1),0.0)':enable='between(t,0.0,6.7697334)',fps=30[trans-video-1];\n" +
                        "[3:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+6.719733/TB[trans-3];\n" +
                        "[4:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+6.719733/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-3];\n" +
                        "[trans-3][video-3]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,6.719733,7.3697333)',fps=30[trans-video-3];\n" +
                        "[5:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+7.319733/TB[trans-5];\n" +
                        "[6:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+7.319733/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-5];\n" +
                        "[trans-5][video-5]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,7.319733,7.969733)',fps=30[trans-video-5];\n" +
                        "[7:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+7.919733/TB[trans-7];\n" +
                        "[8:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+7.919733/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-7];\n" +
                        "[trans-7][video-7]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,7.919733,8.569734)',fps=30[trans-video-7];\n" +
                        "[9:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+8.519733/TB[trans-9];\n" +
                        "[10:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+8.519733/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-9];\n" +
                        "[trans-9][video-9]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,8.519733,9.169734)',fps=30[trans-video-9];\n" +
                        "[11:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+9.119734/TB[trans-11];\n" +
                        "[12:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+9.119734/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-11];\n" +
                        "[trans-11][video-11]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,9.119734,9.769734)',fps=30[trans-video-11];\n" +
                        "[13:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+9.719734/TB[trans-13];\n" +
                        "[14:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+9.719734/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-13];\n" +
                        "[trans-13][video-13]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,9.719734,10.369735)',fps=30[trans-video-13];\n" +
                        "[15:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+10.319735/TB[trans-15];\n" +
                        "[16:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+10.319735/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-15];\n" +
                        "[trans-15][video-15]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,10.319735,10.969735)',fps=30[trans-video-15];\n" +
                        "[17:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+10.919735/TB[trans-17];\n" +
                        "[18:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+10.919735/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-17];\n" +
                        "[trans-17][video-17]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,10.919735,11.569736)',fps=30[trans-video-17];\n" +
                        "[19:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+11.519735/TB[trans-19];\n" +
                        "[20:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+11.519735/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-19];\n" +
                        "[trans-19][video-19]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,11.519735,12.169736)',fps=30[trans-video-19];\n" +
                        "[21:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+12.119736/TB[trans-21];\n" +
                        "[22:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+12.119736/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-21];\n" +
                        "[trans-21][video-21]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,12.119736,12.769736)',fps=30[trans-video-21];\n" +
                        "[23:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+12.719736/TB[trans-23];\n" +
                        "[24:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+12.719736/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-23];\n" +
                        "[trans-23][video-23]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,12.719736,13.369737)',fps=30[trans-video-23];\n" +
                        "[25:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+13.3197365/TB[trans-25];\n" +
                        "[26:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+13.3197365/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-25];\n" +
                        "[trans-25][video-25]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,13.3197365,13.969737)',fps=30[trans-video-25];\n" +
                        "[27:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+13.919737/TB[trans-27];\n" +
                        "[28:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+13.919737/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-27];\n" +
                        "[trans-27][video-27]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,13.919737,14.569737)',fps=30[trans-video-27];\n" +
                        "[29:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+14.519737/TB[trans-29];\n" +
                        "[30:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+14.519737/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-29];\n" +
                        "[trans-29][video-29]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,14.519737,15.169738)',fps=30[trans-video-29];\n" +
                        "[31:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+15.119738/TB[trans-31];\n" +
                        "[32:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+15.119738/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-31];\n" +
                        "[trans-31][video-31]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,15.119738,15.769738)',fps=30[trans-video-31];\n" +
                        "[33:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+15.719738/TB[trans-33];\n" +
                        "[34:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+15.719738/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-33];\n" +
                        "[trans-33][video-33]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,15.719738,16.369738)',fps=30[trans-video-33];\n" +
                        "[35:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+16.319738/TB[trans-35];\n" +
                        "[36:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+16.319738/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-35];\n" +
                        "[trans-35][video-35]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,16.319738,16.969738)',fps=30[trans-video-35];\n" +
                        "[37:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+16.919739/TB[trans-37];\n" +
                        "[38:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+16.919739/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-37];\n" +
                        "[trans-37][video-37]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,16.919739,17.569738)',fps=30[trans-video-37];\n" +
                        "[39:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+17.51974/TB[trans-39];\n" +
                        "[40:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+17.51974/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-39];\n" +
                        "[trans-39][video-39]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,17.51974,18.169739)',fps=30[trans-video-39];\n" +
                        "[41:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+18.11974/TB[trans-41];\n" +
                        "[42:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+18.11974/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-41];\n" +
                        "[trans-41][video-41]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,18.11974,18.76974)',fps=30[trans-video-41];\n" +
                        "[43:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+18.71974/TB[trans-43];\n" +
                        "[44:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+18.71974/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-43];\n" +
                        "[trans-43][video-43]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,18.71974,19.36974)',fps=30[trans-video-43];\n" +
                        "[45:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+19.31974/TB[trans-45];\n" +
                        "[46:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+19.31974/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-45];\n" +
                        "[trans-45][video-45]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,19.31974,19.96974)',fps=30[trans-video-45];\n" +
                        "[47:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+19.91974/TB[trans-47];\n" +
                        "[48:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+19.91974/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-47];\n" +
                        "[trans-47][video-47]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,19.91974,20.56974)',fps=30[trans-video-47];\n" +
                        "[49:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+20.519741/TB[trans-49];\n" +
                        "[50:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+20.519741/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-49];\n" +
                        "[trans-49][video-49]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,20.519741,21.16974)',fps=30[trans-video-49];\n" +
                        "[51:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+21.119741/TB[trans-51];\n" +
                        "[52:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+21.119741/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-51];\n" +
                        "[trans-51][video-51]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,21.119741,21.769741)',fps=30[trans-video-51];\n" +
                        "[53:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+21.719742/TB[trans-53];\n" +
                        "[54:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+21.719742/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-53];\n" +
                        "[trans-53][video-53]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,21.719742,22.369741)',fps=30[trans-video-53];\n" +
                        "[55:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+22.319742/TB[trans-55];\n" +
                        "[56:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+22.319742/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-55];\n" +
                        "[trans-55][video-55]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,22.319742,22.969742)',fps=30[trans-video-55];\n" +
                        "[57:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+22.919743/TB[trans-57];\n" +
                        "[58:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+22.919743/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-57];\n" +
                        "[trans-57][video-57]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,22.919743,23.569742)',fps=30[trans-video-57];\n" +
                        "[59:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+23.519743/TB[trans-59];\n" +
                        "[60:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+23.519743/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-59];\n" +
                        "[trans-59][video-59]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,23.519743,24.169743)',fps=30[trans-video-59];\n" +
                        "[61:v]trim=duration=0.65000004,setpts=PTS-STARTPTS+24.119743/TB[trans-61];\n" +
                        "[62:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+24.119743/TB',trim=duration=0.65000004,tpad=stop_mode=clone:stop_duration=0.05[video-61];\n" +
                        "[trans-61][video-61]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,24.119743,24.769743)',fps=30[trans-video-61];\n" +
                        "[63:v]trim=duration=0.6,setpts=PTS-STARTPTS+24.719744/TB[trans-63];\n" +
                        "[64:v]scale=iw*1.0:ih*1.0,rotate='if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)':ow=rotw('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):oh=roth('if(between(t,0.04352188,0.5805588),0.05235988+(0.0-0.05235988)*clip((t-0.04352188)/0.5370369,0,1),0.0)'):fillcolor=0x00000000,format=rgba,colorchannelmixer=aa=1.0,zoompan=z=zoom*'if(between(it,0.04352188,0.5805588),1.2+(1.0-1.2)*clip((it-0.04352188)/0.5370369,0,1),1.0)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',setpts='(PTS-STARTPTS)/1.0+24.719744/TB',trim=duration=0.6,tpad=stop_mode=clone:stop_duration=0.0[video-63];\n" +
                        "[trans-63][video-63]overlay='if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':'if(between(t,0.04352188,0.5805588),0.0+(0.0-0.0)*clip((t-0.04352188)/0.5370369,0,1),0.0)':enable='between(t,24.719744,25.319744)',fps=30[trans-video-63];\n" +
                        "[65:a]atrim=start=218.25166:end=240.56781,adelay=40|40,asetpts=PTS-STARTPTS[audio-65];\n" +
                        "[66:v]trim=duration=21.977732,setpts=PTS-STARTPTS+0.0/TB[trans-66];\n" +
                        "[trans-66]drawtext=fontfile='/system/fonts/DroidSans.ttf':fontsize=125.0:text='Recap 2025':x=(w-text_w)/2:y=(h-text_h)/2:enable='between(t,0.0,21.977732)',fps=30[trans-video-66];\n" +
                        "[trans-video-1][trans-video-3]xfade=transition=fade:duration=0.1:offset=6.6197333[phase32];\n" +
                        "[phase32][trans-video-5]xfade=transition=fade:duration=0.1:offset=7.1197333[phase31];\n" +
                        "[phase31][trans-video-7]xfade=transition=fade:duration=0.1:offset=7.6197333[phase30];\n" +
                        "[phase30][trans-video-9]xfade=transition=fade:duration=0.1:offset=8.119733[phase29];\n" +
                        "[phase29][trans-video-11]xfade=transition=fade:duration=0.1:offset=8.619733[phase28];\n" +
                        "[phase28][trans-video-13]xfade=transition=fade:duration=0.1:offset=9.119733[phase27];\n" +
                        "[phase27][trans-video-15]xfade=transition=fade:duration=0.1:offset=9.619733[phase26];\n" +
                        "[phase26][trans-video-17]xfade=transition=fade:duration=0.1:offset=10.119733[phase25];\n" +
                        "[phase25][trans-video-19]xfade=transition=fade:duration=0.1:offset=10.619733[phase24];\n" +
                        "[phase24][trans-video-21]xfade=transition=fade:duration=0.1:offset=11.119733[phase23];\n" +
                        "[phase23][trans-video-23]xfade=transition=fade:duration=0.1:offset=11.619733[phase22];\n" +
                        "[phase22][trans-video-25]xfade=transition=fade:duration=0.1:offset=12.119733[phase21];\n" +
                        "[phase21][trans-video-27]xfade=transition=fade:duration=0.1:offset=12.619733[phase20];\n" +
                        "[phase20][trans-video-29]xfade=transition=fade:duration=0.1:offset=13.119733[phase19];\n" +
                        "[phase19][trans-video-31]xfade=transition=fade:duration=0.1:offset=13.619733[phase18];\n" +
                        "[phase18][trans-video-33]xfade=transition=fade:duration=0.1:offset=14.119733[phase17];\n" +
                        "[phase17][trans-video-35]xfade=transition=fade:duration=0.1:offset=14.619733[phase16];\n" +
                        "[phase16][trans-video-37]xfade=transition=fade:duration=0.1:offset=15.119733[phase15];\n" +
                        "[phase15][trans-video-39]xfade=transition=fade:duration=0.1:offset=15.619733[phase14];\n" +
                        "[phase14][trans-video-41]xfade=transition=fade:duration=0.1:offset=16.119732[phase13];\n" +
                        "[phase13][trans-video-43]xfade=transition=fade:duration=0.1:offset=16.619732[phase12];\n" +
                        "[phase12][trans-video-45]xfade=transition=fade:duration=0.1:offset=17.119732[phase11];\n" +
                        "[phase11][trans-video-47]xfade=transition=fade:duration=0.1:offset=17.619732[phase10];\n" +
                        "[phase10][trans-video-49]xfade=transition=fade:duration=0.1:offset=18.119732[phase9];\n" +
                        "[phase9][trans-video-51]xfade=transition=fade:duration=0.1:offset=18.619732[phase8];\n" +
                        "[phase8][trans-video-53]xfade=transition=fade:duration=0.1:offset=19.119732[phase7];\n" +
                        "[phase7][trans-video-55]xfade=transition=fade:duration=0.1:offset=19.619732[phase6];\n" +
                        "[phase6][trans-video-57]xfade=transition=fade:duration=0.1:offset=20.119732[phase5];\n" +
                        "[phase5][trans-video-59]xfade=transition=fade:duration=0.1:offset=20.619732[phase4];\n" +
                        "[phase4][trans-video-61]xfade=transition=fade:duration=0.1:offset=21.119732[phase3];\n" +
                        "[phase3][trans-video-63]xfade=transition=fade:duration=0.1:offset=21.619732[phase2];\n" +
                        "[base][phase2]overlay=enable='between(t,0.0,22.219732)'[layer-0];\n" +
                        "[layer-0][trans-video-66]overlay=enable='between(t,0.0,21.977732)'[layer-1];\n" +
                        "[audio-65]amix=inputs=1:dropout_transition=0[aout];\n" +
                        "\" -map \"[layer-1]\" -map \"[aout]\" -c:v libx264 -preset ultrafast -tune zerolatency -crf 30 -y \"<output.mp4>\"",
                "https://app.vanvatcorp.com/doubleclips/templates/viet2007ht/dvtr5_gdGER5/preview.png",
                "https://app.vanvatcorp.com/doubleclips/templates/viet2007ht/dvtr5_gdGER5/preview.mp4",
                new Date().getTime(), 8032007, 32, new String[]{"thucgiac.mp3"});
        addTemplate(data1);

//        TemplateData data = new TemplateData(projectPath, projectName, new Date().getTime(), 31122007, 8032007);
//        data.version = BuildConfig.VERSION_NAME;
//        projectList.add(data);
//        projectAdapter.notifyItemInserted(projectList.size() - 1);
//
//        File basicDir = new File(IOHelper.CombinePath(projectPath, Constants.DEFAULT_CLIP_TEMP_DIRECTORY, "frames"));
//        if(!basicDir.exists())
//            basicDir.mkdirs();
//
//        File previewDir = new File(IOHelper.CombinePath(projectPath, Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY));
//        if(!previewDir.exists())
//            previewDir.mkdirs();
//
//        enterEditing(getContext(), data);
    }

    public void reloadingProject()
    {
        // TODO: Reload from real server, this serve as example purpose only.
        fetchTemplate();


//        projectList.clear();
//        projectAdapter.notifyDataSetChanged();
//        String projectsFolderPath = IOHelper.CombinePath(IOHelper.getPersistentDataPath(getContext()), "projects");
//        File file = new File(projectsFolderPath);
//        if(file.listFiles() == null) {
//            projectSwipeRefreshLayout.setRefreshing(false);
//            return;
//        }
//        for (File directory : Objects.requireNonNull(file.listFiles())) {
//            if(directory.isDirectory())
//            {
//                TemplateData data = TemplateData.loadProperties(getContext(), directory.getAbsolutePath());
//
//                if(data != null)
//                {
//                    projectList.add(data);
//                    projectAdapter.notifyItemInserted(projectList.size() - 1);
//                }
//            }
//
//        }
//
        templateSwipeRefreshLayout.setRefreshing(false);
    }










    public static class TemplateData implements Serializable {

        public String version;
        private String templateAuthor;
        private String templateId;
        private String projectTitle;
        private String projectDescription;
        private String ffmpegCommand;
        private String templateSnapshotLink;
        private String templateVideoLink;
        private long templateTimestamp;
        private long templateDuration;
        private int templateTotalClip;
        private String[] additionalResourceName;

        public TemplateData(String templateAuthor, String templateId, String projectTitle, String projectDescription, String ffmpegCommand, String templateSnapshotLink, String templateVideoLink, long templateTimestamp, long templateDuration, int templateTotalClip, String[] additionalResourceName) {
            this.templateAuthor = templateAuthor;
            this.templateId = templateId;
            this.projectTitle = projectTitle;
            this.projectDescription = projectDescription;
            this.ffmpegCommand = ffmpegCommand;
            this.templateSnapshotLink = templateSnapshotLink;
            this.templateVideoLink = templateVideoLink;
            this.templateTimestamp = templateTimestamp;
            this.templateDuration = templateDuration;
            this.templateTotalClip = templateTotalClip;
            this.additionalResourceName = additionalResourceName;
        }


        public String getTemplateAuthor() {
            return templateAuthor;
        }
        public String getTemplateId() {
            return templateId;
        }
        public String getProjectTitle() {
            return projectTitle;
        }
        public String getProjectDescription() {
            return projectDescription;
        }
        public String getFfmpegCommand() {
            return ffmpegCommand;
        }
        public String getTemplateSnapshotLink() {
            return templateSnapshotLink;
        }
        public String getTemplateVideoLink() {
            return templateVideoLink;
        }
        public long getTemplateTimestamp() {
            return templateTimestamp;
        }
        public long getTemplateDuration() {
            return templateDuration;
        }
        public int getTemplateClipCount() {
            return templateTotalClip;
        }
        public String[] getTemplateAdditionalResourcesName() {
            return additionalResourceName;
        }

        public String getTemplateLocation() {
            return "/" + templateAuthor + "/" + templateId;
        }
    }
    public class TemplateDataAdapter extends RecyclerView.Adapter<TemplateDataViewHolder>
    {

        private List<TemplateData> templateList;
        private Context context;

        // Constructor
        public TemplateDataAdapter(Context context, List<TemplateData> templateList) {
            this.context = context;
            this.templateList = templateList;
        }
        @Override
        public TemplateDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.cpn_template_element, parent, false);
            return new TemplateDataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TemplateDataViewHolder holder, int position) {

            TemplateData projectItem = templateList.get(position);

            holder.templateTitle.setText(projectItem.getProjectTitle());

            ImageHelper.getImageBitmapFromNetwork(context, projectItem.getTemplateSnapshotLink(), holder.templatePreview);
            ViewGroup.LayoutParams imageDimension = holder.templatePreview.getLayoutParams();
            imageDimension.width = ViewGroup.LayoutParams.MATCH_PARENT;
            imageDimension.height = Random.Range(100, 600);
            holder.templatePreview.setLayoutParams(imageDimension);
            holder.templateTitle.setOnClickListener(v -> {
                holder.wholeView.performClick();
            });


            holder.wholeView.setOnClickListener(v -> {
                Intent intent = new Intent(context, TemplatePreviewActivity.class);
                intent.putExtra("TemplateData", projectItem);
                context.startActivity(intent);
            });
            holder.wholeView.setOnLongClickListener(v -> {
                return true;
            });
        }


        @Override
        public int getItemCount() {
            return templateList.size();
        }
    }
    public static class TemplateDataViewHolder extends RecyclerView.ViewHolder {
        TextView templateTitle;
        ImageView templatePreview;
        View wholeView;
        public TemplateDataViewHolder(@NonNull View itemView) {
            super(itemView);
            wholeView = itemView;

            templateTitle = itemView.findViewById(R.id.titleText);
            templatePreview = itemView.findViewById(R.id.previewImage);
        }
    }
}
