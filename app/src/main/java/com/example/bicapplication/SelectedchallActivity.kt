package com.example.bicapplication

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bicapplication.certify.CertifyStatusActivity
import com.example.bicapplication.databinding.ActivitySelectedchallBinding
import com.example.bicapplication.datamodel.ChallData
import com.google.firebase.FirebaseApp
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase

class SelectedchallActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectedchallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectedchallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()

        //유저가 카톡 초대링크로 이 액티비티 온것인지 확인 후 처리
        checkInviteLink()

        //친구 초대하기 버튼 클릭시
        binding.Invitebutton.setOnClickListener {
            invite()
        }

    }

    private fun initLayout() {

        binding.apply {
            btnSelectedBack.setOnClickListener {
                finish()
            }
            btnChallParticipate.setOnClickListener {
                if (challData?.isProgress == 0) {
                    ParticipateActivity.challData = challData
                    val intent = Intent(this@SelectedchallActivity, ParticipateActivity::class.java)
                    startActivity(intent)
                } else if (challData?.isProgress == 1) {
                    val intent = Intent(this@SelectedchallActivity, CertifyStatusActivity::class.java)
                    startActivity(intent)
                }
            }

            if (challData?.isProgress == 0) {
                btnChallParticipate.text = "참가하기"
            } else if (challData?.isProgress == 1) {
                btnChallParticipate.text = "인증하기"
            }

            when (challData?.authMethod) {
                1 -> textAuthMethod.text = "이미지 인증"
                2 -> textAuthMethod.text = "깃허브 인증"
                3 -> textAuthMethod.text = "액션 인증"
            }
            when (challData?.isPublic) {
                true -> textIsPublic.text = "공개"
                false -> textIsPublic.text = "비공개"
                else -> {}
            }
            textChallName.text = challData?.challName
            textTotaldays.text = challData?.totalDays.toString()
            textviewFinishedDesc.text = challData?.challDesc
            textCategory.text = challData?.category
            textPeriod.text = challData?.enddate
            textMoney.text = challData?.money.toString()
            textUserNum.text = challData?.userNum.toString()
        }
    }

    companion object {
        var challData: ChallData? = null
    }

    //카톡 초대하기로 해당 액티비티 온 경우 처리 (딥링크)
    private fun checkInviteLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                // 쿼리로 초대한 유저의 uid가 존재한다면
                if (deepLink != null &&
                    deepLink.getBooleanQueryParameter("uid", false)
                ) {
//                    /*
//                    * 챌린지 참가자 리스트에 유저 추가하는 로직 등 필요하면 넣기
//                    * */
//                    val referrerUserId = deepLink.getQueryParameter("uid")
//                    Toast.makeText(this, "$referrerUserId", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //카톡앱 실행해서 링크 공유
    private fun sendInviteLink(inviteLink: Uri) {
        val userName = "차차차" // 초대해주는 유저이름 (로컬저장소에서 유저정보 가져와서 써주기)
        val inviteIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // 고정 text
            setPackage("com.kakao.talk") // 카카오톡 패키지 지정
            // 초대 코드 텍스트 지정
            putExtra(
                Intent.EXTRA_TEXT,
                "${userName}님이 챌린지에 초대하였습니다! 챌린지에 도전해보세요.\n\n[챌린지 링크] : $inviteLink"
            )
        }
        try {
            startActivity(inviteIntent) // 수업 초대를 위해 카카오톡 실행
        } catch (e: ActivityNotFoundException) {
            // 카카오톡이 설치되어 있지 않은 경우 예외 발생
            Toast.makeText(this, "카카오톡이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //파베의 다이나믹 링크 확인 후 sendInviteLink 메소드 실행
    private fun invite() {
        val userId = "kim1234" // Query로 사용할 유저 아이디 (로컬저장소에서 유저정보 가져와서 써주기) (필요없으면 나중에 지워주기)

        // (Manifest에 설정한 scheme, host, path와 동일해야 함.)
        val invitationLink = "https://bicapplication.page.link/invite?uid=$userId" // 생성할 다이나믹 링크

        val dynamicLink =
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(invitationLink))
                .setDomainUriPrefix("https://bicapplication.page.link") // 파이어베이스 다이나믹 링크란에 설정한 Prefix 입력
                .setAndroidParameters(
                    DynamicLink.AndroidParameters.Builder().build()
                )
                .buildShortDynamicLink()

        dynamicLink.addOnSuccessListener { task ->
            val inviteLink = task.shortLink!!
            sendInviteLink(inviteLink)
        }

    }






}