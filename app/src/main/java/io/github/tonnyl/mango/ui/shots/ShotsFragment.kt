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

package io.github.tonnyl.mango.ui.shots

import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import io.github.tonnyl.mango.R
import io.github.tonnyl.mango.data.Shot
import io.github.tonnyl.mango.data.User
import io.github.tonnyl.mango.glide.GlideLoader
import io.github.tonnyl.mango.ui.auth.AuthActivity
import io.github.tonnyl.mango.ui.settings.SettingsActivity
import io.github.tonnyl.mango.ui.shot.ShotActivity
import io.github.tonnyl.mango.ui.shot.ShotPresenter
import io.github.tonnyl.mango.ui.user.UserProfileActivity
import io.github.tonnyl.mango.ui.user.UserProfilePresenter
import io.github.tonnyl.mango.util.AccessTokenManager
import io.github.tonnyl.mango.util.Constants
import kotlinx.android.synthetic.main.fragment_shots.*
import kotlinx.android.synthetic.main.fragment_simple_list.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.startActivity

/**
 * Created by lizhaotailang on 2017/6/29.
 *
 * Main ui for the shots page screen.
 */

class ShotsFragment : Fragment(), ShotsContract.View {

    private lateinit var mPresenter: ShotsContract.Presenter
    private var mAdapter: ShotsAdapter? = null
    private var mIsLoading = false

    companion object {
        @JvmStatic
        fun newInstance(): ShotsFragment {
            return ShotsFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_shots, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        mPresenter.subscribe()

        user_name.setOnClickListener {
            mPresenter.getUser()?.let {
                context?.startActivity<UserProfileActivity>(UserProfilePresenter.EXTRA_USER to it)
            }
        }

        refresh_layout.setOnRefreshListener {
            mIsLoading = true
            mPresenter.listShots()
        }

        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView?.layoutManager as LinearLayoutManager
                if (dy > 0 && layoutManager.findLastVisibleItemPosition() == recycler_view.adapter.itemCount - 1 && !mIsLoading) {
                    mPresenter.listMoreShots()
                    mIsLoading = true
                }
            }
        })

        setHasOptionsMenu(true)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter.unsubscribe()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.action_logout -> {
                showLogoutDialog()
            }
            R.id.action_settings -> {
                context?.startActivity<SettingsActivity>()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setPresenter(presenter: ShotsContract.Presenter) {
        mPresenter = presenter
    }

    override fun initViews() {
        with(activity as MainActivity) {
            setSupportActionBar(toolbar)
        }

        context?.let { refresh_layout.setColorSchemeColors(ContextCompat.getColor(it, R.color.colorAccent)) }
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(context)
    }

    override fun setLoadingIndicator(loading: Boolean) {
        refresh_layout.post({
            refresh_layout.isRefreshing = loading
        })
    }

    override fun showResults(results: List<Shot>) {
        if (mAdapter == null) {
            context?.let { mAdapter = ShotsAdapter(it, results) }
            mAdapter?.setItemClickListener(object : OnRecyclerViewItemClickListener {

                override fun onItemClick(view: View, position: Int) {
                    context?.startActivity<ShotActivity>(ShotPresenter.EXTRA_SHOT to results[position])
                }

                override fun onAvatarClick(view: View, position: Int) {
                    results[position].user?.let {
                        context?.startActivity<UserProfileActivity>(UserProfilePresenter.EXTRA_USER to it)
                    }
                }

            })
            recycler_view.adapter = mAdapter
        }

        mIsLoading = false

    }

    override fun showNetworkError() {
        Snackbar.make(recycler_view, R.string.network_error, Snackbar.LENGTH_SHORT).show()
    }

    override fun setEmptyContentVisibility(visible: Boolean) {
        empty_view.visibility = if (visible && (mAdapter == null)) View.VISIBLE else View.GONE
    }

    override fun notifyDataAllRemoved(size: Int) {
        mAdapter?.notifyItemRangeRemoved(0, size)
        mIsLoading = false
    }

    override fun notifyDataAdded(startPosition: Int, size: Int) {
        mAdapter?.notifyItemRangeInserted(startPosition, size)
        mIsLoading = false
    }

    override fun showAuthUserInfo(user: User) {
        user_name.text = user.name
        GlideLoader.loadAvatar(user_name, user.avatarUrl)
    }

    override fun disableShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            activity?.getSystemService(ShortcutManager::class.java)
                    ?.disableShortcuts(arrayListOf(Constants.SHORTCUT_ID_POPULAR,
                            Constants.SHORTCUT_ID_FOLLOWING,
                            Constants.SHORTCUT_ID_RECENT,
                            Constants.SHORTCUT_ID_DEBUTS))
        }
    }

    override fun navigateToLogin() {
        AccessTokenManager.clear()
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(Constants.IS_USER_LOGGED_IN, false)
                .putString(Constants.ACCESS_TOKEN, null)
                .apply()
        context?.let { it.startActivity(it.intentFor<AuthActivity>().newTask().clearTask()) }
        activity?.finish()
    }

    private fun showLogoutDialog() {
        context?.let {
            AlertDialog.Builder(it)
                    .setTitle(R.string.log_out)
                    .setMessage(getString(R.string.logout_message))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        mPresenter.logoutUser()
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ ->

                    }
                    .show()
        }
    }

}