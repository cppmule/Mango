/*
 * Copyright (c) 2017 Lizhaotailang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.github.tonnyl.mango.ui.user

import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.Html
import android.view.*
import io.github.tonnyl.mango.R
import io.github.tonnyl.mango.data.User
import io.github.tonnyl.mango.glide.GlideLoader
import kotlinx.android.synthetic.main.fragment_user_profile.*
import org.jetbrains.anko.browse

/**
 * Created by lizhaotailang on 2017/6/28.
 *
 * Main ui for the user profile screen.
 */

class UserProfileFragment : Fragment(), UserProfileContract.View {

    private lateinit var mPresenter: UserProfileContract.Presenter
    private var mFollowable = false
    private var mIsFollowing = false

    companion object {

        @JvmStatic
        fun getInstance(): UserProfileFragment {
            return UserProfileFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        mPresenter.subscribe()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter.unsubscribe()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_user, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val menuItem = menu?.getItem(0)?.subMenu?.getItem(0)
        menuItem?.isVisible = mFollowable
        if (mIsFollowing) {
            menuItem?.setIcon(R.drawable.ic_user_minus_black_24dp)
            menuItem?.setTitle(R.string.unfollow)
        } else {
            menuItem?.setIcon(R.drawable.ic_user_plus_black_24dp)
            menuItem?.setTitle(R.string.follow)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id) {
            android.R.id.home -> activity?.onBackPressed()
            R.id.action_follow_unfollow -> mPresenter.toggleFollow()
            R.id.action_open_in_browser -> context?.browse(mPresenter.getUser().htmlUrl)
        }
        activity?.invalidateOptionsMenu()
        return true
    }

    override fun setPresenter(presenter: UserProfileContract.Presenter) {
        mPresenter = presenter
    }

    override fun showUserInfo(user: User) {
        GlideLoader.loadAvatar(avatar, user.avatarUrl)

        user.links.twitter?.let {
            twitterTextView.text = it
        } ?: run {
            twitterTextView.visibility = View.GONE
        }

        user.links.web?.let {
            webTextView.text = it
        } ?: run {
            webTextView.visibility = View.GONE
        }

        user.location?.let {
            locationTextView.text = it
        } ?: run {
            locationTextView.visibility = View.GONE
        }

        if (Build.VERSION.SDK_INT >= 24) {
            bioTextView.text = Html.fromHtml(user.bio, Html.FROM_HTML_MODE_LEGACY)
        } else {
            bioTextView.text = Html.fromHtml(user.bio)
        }

        nameTextView.text = user.name
        usernameTextView.text = user.login
    }

    override fun setFollowing(isFollowing: Boolean) {
        mIsFollowing = isFollowing
        activity?.invalidateOptionsMenu()
    }

    override fun setFollowable(followable: Boolean) {
        mFollowable = followable
        activity?.invalidateOptionsMenu()
    }

    override fun showNetworkError() {
        Snackbar.make(avatar, R.string.network_error, Snackbar.LENGTH_SHORT).show()
    }

    private fun initViews() {
        with(activity as UserProfileActivity) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

}