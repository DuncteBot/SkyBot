## Setting up your Environment


1. Create a Fork (If you already have a local repository skip to step 3)
    
    ![Create Fork](https://i.imgur.com/kQ9QRSO.png)

2. Clone Repository
    ```bash
     $ git clone https://github.com/ExampleName/SkyBot.git
    Cloning into 'SkyBot'...
    remote: Counting objects: 15377, done.
    remote: Total 15377 (delta 0), reused 0 (delta 0), pack-reused 15377
    Receiving objects: 100% (15377/15377), 21.64 MiB | 2.36 MiB/s, done.
    Resolving deltas: 100% (8584/8584), done.
    Checking connectivity... done.
    ```
> Replace `ExampleName` with your GitHub username, mine for example is `duncte123`

3. Move to your local repository (here `SkyBot`)

4. Configure upstream remote to keep your fork updated
    ```bash
     $ git remote add upstream https://github.com/duncte123/SkyBot.git
    ```

5. Create branch based on `upstream/master`
    ```bash
    $ git checkout -b patch-1 upstream/master
    Switched to a new branch 'upstream/master'
    ```

## Making Changes

Depending on your changes there are certain rules you have to follow if you expect
your Pull Request to be merged.

**Note**: It is recommended to create a new remote branch for each Pull Request. 
Based on the current `upstream/master` changes!

1. Adding a new Method or Class
    - If your addition is not internal (e.g. an impl class or private method) you have to write documentation.
        - For that we like to follow [this structure guide](https://github.com/DuncteBot/SkyBot/wiki/Structure-Guide#javadoc)
        Our structure guide has been adapted from [JDA's structure guide](https://github.com/DV8FromTheWorld/JDA/wiki/6%29-JDA-Structure-Guide#javadoc)
        - If you are using Intellij IDEA you can import the code styles that are included in the repo
    - Keep your code consistent! [example](#examples)
        - We use 4 spaces insted of tabs
        - Compare your code style to the one used all over SkyBot and ensure you
          do not break the consistency (if you find issues in the SkyBot style you can include and update it)

2. Making a Commit
    - While having multiple commits can help the reader understand your changes, it might sometimes be
      better to include more changes in a single commit.
    - When you commit your changes write a proper commit caption which explains what you have done

3. Updating your Fork
    - Before you start committing make sure your fork is updated.
      (See [Syncing a Fork](https://help.github.com/articles/syncing-a-fork/)
      or [Keeping a Fork Updated](https://robots.thoughtbot.com/keeping-a-github-fork-updated))

## Creating a Pull Request

1. Commit your changes
    ```bash
    $ git commit -m "Updated Copyright in build.gradle"
    [patch-1 340383d] Updated Copyright in build.gradle
    1 file changed, 1 insertion(+), 1 deletion(-)
    ```

2. Push your commits
    ```bash
    $ git push origin patch-1:patch-1
    Counting objects: 3, done.
    Delta compression using up to 8 threads.
    Compressing objects: 100% (3/3), done.
    Writing objects: 100% (3/3), 313 bytes | 0 bytes/s, done.
    Total 3 (delta 2), reused 0 (delta 0)
    remote: Resolving deltas: 100% (2/2), completed with 2 local objects.
    To https://github.com/ExampleName/SkyBot.git
     * [new branch]      patch-1 -> patch-1
    ```

3. Open Pull-Request

    ![open pull request](https://i.imgur.com/iZQNPJS.png)

4. Set base branch to 
    `base fork: duncte123/SkyBot` `base: development`

5. Allow edits from Maintainers

6. Done! Just click **Create pull request** and await a review by one of the maintainers!

![Example Pull-Request](https://i.imgur.com/0HywAuP.png)

### Examples

***
**Bad Addition**
```diff
+   public static Response postRequest(String url, Map<String, Object> postFields) 
+   {
+       if (url != null)
+           return null;
+       
+       return postRequest(url, postFields, AcceptType.URLENCODED);
+   }
```

**Good Addition**
```diff
+    /**
+    * This makes a post request to the specified website
+    * @param url The website to post to
+    * @param postFields the params for the post
+    * @return The {@link okhttp3.Response Response} from the webserver
+    */
+   @Nullable
+   public static Response postRequest(String url, @Nonnull Map<String, Object> postFields) {
+       if (url != null) {
+           return null;
+       }
+
+       return postRequest(url, postFields, AcceptType.URLENCODED);
+   }
```
The annotations should always be imported from `org.jetbrains.annotations`
