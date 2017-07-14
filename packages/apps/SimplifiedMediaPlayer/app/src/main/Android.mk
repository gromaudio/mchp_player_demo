#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := SimplifiedMediaPlayer

LOCAL_MODULE_TAGS := optional

#LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 guava
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-appcompat
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-gridlayout
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-recyclerview
#LOCAL_STATIC_JAVA_LIBRARIES += android-support-v13

LOCAL_SRC_FILES := $(call all-java-files-under, java) $(call all-Iaidl-files-under, java)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/v7/appcompat/res
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/v7/gridlayout/res
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/v7/recyclerview/res

	
LOCAL_AIDL_INCLUDES := $(LOCAL_PATH)/java

LOCAL_CERTIFICATE := platform
#LOCAL_SDK_VERSION := current
LOCAL_PROGUARD_ENABLED := disabled

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat:android.support.v7.gridlayout:android.support.v7.recyclerview




include $(BUILD_PACKAGE)

