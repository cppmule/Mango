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

package io.github.tonnyl.mango.data

import android.annotation.SuppressLint
import android.arch.persistence.room.*
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.github.tonnyl.mango.database.converter.DateConverter
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by lizhaotailang on 2017/6/25.
 *
 * {
 * "id" : 1,
 * "name" : "Dan Cederholm",
 * "username" : "simplebits",
 * "html_url" : "https://dribbble.com/simplebits",
 * "avatar_url" : "https://d13yacurqjgara.cloudfront.net/users/1/avatars/normal/dc.jpg?1371679243",
 * "bio" : "Co-founder &amp; designer of <a href=\"https://dribbble.com/dribbble\">@Dribbble</a>. Principal of SimpleBits. Aspiring clawhammer banjoist.",
 * "location" : "Salem, MA",
 * "links" : {
 * "web" : "http://simplebits.com",
 * "twitter" : "https://twitter.com/simplebits"
 * },
 * "can_upload_shot" : true,
 * "pro" : true,
 * "created_at" : "2009-07-08T02:51:22Z",
 * "type" : "User",
 * "teams" : [
 * {
 * "id" : 39,
 * "name" : "Dribbble",
 * "username" : "dribbble",
 * "html_url" : "https://dribbble.com/dribbble",
 * "avatar_url" : "https://d13yacurqjgara.cloudfront.net/users/39/avatars/normal/apple-flat-precomposed.png?1388527574",
 * "bio" : "Show and tell for designers. This is Dribbble on Dribbble.",
 * "location" : "Salem, MA",
 * "links" : {
 * "web" : "http://dribbble.com",
 * "twitter" : "https://twitter.com/dribbble"
 * },
 * "type" : "Team",
 * "created_at" : "2009-08-18T18:34:31Z",
 * "updated_at" : "2014-02-14T22:32:11Z"
 * }
 * ]
 * }
 */

@Entity(tableName = "user")
@TypeConverters(DateConverter::class)
@Parcelize
@SuppressLint("ParcelCreator")
data class User(
        @ColumnInfo(name = "name")
        @SerializedName("name")
        @Expose
        val name: String,

        @ColumnInfo(name = "login")
        @SerializedName("login")
        @Expose
        val login: String,

        @ColumnInfo(name = "html_url")
        @SerializedName("html_url")
        @Expose
        val htmlUrl: String,

        @ColumnInfo(name = "avatar_url")
        @SerializedName("avatar_url")
        @Expose
        val avatarUrl: String,

        @ColumnInfo(name = "bio")
        @SerializedName("bio")
        @Expose
        val bio: String,

        @SerializedName("location")
        @Expose
        val location: String?,

        @Embedded
        @SerializedName("links")
        @Expose
        val links: Links,

        @ColumnInfo(name = "shots_count")
        @SerializedName("shots_count")
        @Expose
        val shotsCount: Int,

        @ColumnInfo(name = "can_upload_shot")
        @SerializedName("can_upload_shot")
        @Expose
        val canUploadShot: Boolean,

        @ColumnInfo(name = "type")
        @SerializedName("type")
        @Expose
        val type: String,

        @ColumnInfo(name = "pro")
        @SerializedName("pro")
        @Expose
        val pro: Boolean,

        @ColumnInfo(name = "created_at")
        @SerializedName("created_at")
        @Expose
        val createdAt: Date,

        @PrimaryKey
        @ColumnInfo(name = "id")
        @SerializedName("id")
        @Expose
        val id: Long
) : Parcelable